package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.payment.dto.response.TossConfirmResponse;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttemptStatus;
import com.example.studyroomreservation.domain.payment.mapper.PaymentMapper;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.domain.reservation.service.ReservationQueryService;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentTransactionHelper {

    private final PaymentRepository paymentRepository;
    private final ReservationQueryService reservationQueryService;

    private final PaymentMapper paymentMapper;
    private final PaymentAttemptFailService failService;
     private final ReservationRepository reservationRepository;

    private static final long PAYMENT_APPROVE_TTL_MINUTES = 10L;

    @Transactional
    public void appendPayment(PaymentAttempt attempt, TossConfirmResponse confirmResponse) {

        validateFinalStatus(attempt, confirmResponse);

        attempt.markAsSuccess(confirmResponse.paymentKey());

        // 결제 정보 최종 저장 및 예약 확정 (Race Condition 방지용 try-catch)
        try {
            Payment payment = paymentMapper.toPaymentSuccess(attempt, confirmResponse);
            paymentRepository.save(payment);

            // 예약 상태 확정 (TEMP -> CONFIRMED)
            Reservation reservation = reservationRepository.findById(attempt.getReservationId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
            reservation.confirm(LocalDateTime.now());
        } catch (DataIntegrityViolationException e) {
            // Unique 조건 위반으로 잡힘 -> 이미 성공한 것으로 간주하고 리턴
            return;
        }
    }

    private void validateFinalStatus(PaymentAttempt attempt, TossConfirmResponse response) {
        String orderId = attempt.getOrderId();

        // 시간 검증 (TTL)
        if (attempt.getCreatedAt().plusMinutes(PAYMENT_APPROVE_TTL_MINUTES).isBefore(LocalDateTime.now())) {
            failService.markFailed(orderId, "PAYMENT_EXPIRED", "payment attempt expired");
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        // 예약 상태 및 만료 최종 확인 (방어 로직)
        Reservation reservation = reservationQueryService.getById(attempt.getReservationId());

        //TODO : 돈은 나갔는데 예약 상태가 이상한 상황 or 예약이 만료된 상황 -> (추후 환불 로직 cancel 호출 필요)
        try {
            reservation.validatePayableForApprove(LocalDateTime.now());
        } catch (BusinessException e) {
            failService.markFailed(
                    orderId,
                    e.getErrorCode().name(),
                    "Reservation validation failed: " + e.getErrorCode().name()
            );
            throw e;
        }

        // 금액 일치 검증
        if (response.totalAmount() != attempt.getAmount()) {
            failService.markFailed(orderId, "AMOUNT_MISMATCH", "PG amount mismatch");
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        //  멱등성 검증
        if (attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.SUCCESS) {
            // 이미 성공한 요청이라면, 저장된 PaymentKey와 응답받은 PaymentKey가 같은지 확인
            if (attempt.getPaymentKey() != null && attempt.getPaymentKey().equals(response.paymentKey())) {
                return;
            }
            throw new BusinessException(ErrorCode.PAYMENT_DUPLICATE_APPROVE);
        }

        // 결제 상태 전이 검증
        if (attempt.getPaymentAttemptStatus() != PaymentAttemptStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }
    }
}