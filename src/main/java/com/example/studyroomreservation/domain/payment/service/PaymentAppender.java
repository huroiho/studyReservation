package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.dto.response.TossConfirmResponse;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttemptStatus;
import com.example.studyroomreservation.domain.payment.mapper.PaymentMapper;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.payment.service.PaymentAttemptFailService;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
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
public class PaymentAppender {
    //PaymentAppender 네이밍 이유: Appender는 보통 DDD나 CQS 패턴에서 데이터 저장(Insert) 행위에만 집중하는 컴포넌트를 의미, 프록시의 자가호출 문제 해결을 위해 생성

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final ReservationRepository reservationRepository;

    private final PaymentMapper paymentMapper;
    private final PaymentAttemptFailService failService;
     private final ReservationService reservationService;

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
            reservationService.confirmReservation(attempt.getReservationId());

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
        Reservation reservation = reservationRepository.findById(attempt.getReservationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.TEMP) {
            // 돈은 나갔는데 예약 상태가 이상한 상황 -> (추후 환불 로직 cancel 호출 필요)
            failService.markFailed(orderId, "INVALID_STATUS", "Reservation is not TEMP");
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS);
        }

        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            //TODO 돈은 나갔는데 예약이 만료된 상황 -> (추후 환불 로직 cancel 호출 필요)
            failService.markFailed(orderId, "RESERVATION_EXPIRED", "Reservation time has expired before save");
            throw new BusinessException(ErrorCode.RES_ALREADY_EXPIRED);
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