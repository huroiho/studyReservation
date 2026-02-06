package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.domain.payment.client.TossPaymentsClient;
import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.dto.request.TossConfirmRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.dto.response.TossConfirmResponse;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttemptStatus;
import com.example.studyroomreservation.domain.payment.entity.PaymentMethod;
import com.example.studyroomreservation.domain.payment.mapper.PaymentMapper;
import com.example.studyroomreservation.domain.payment.mapper.TossConfirmMapper;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.aop.DistributedLock;
import com.example.studyroomreservation.global.config.TossPaymentConfig;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentAttemptRepository paymentAttemptRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    private final PaymentTransactionHelper paymentTransactionHelper;
    private final PaymentAttemptFailService failService;
    private final TossPaymentsClient tossPaymentsClient;

    private final TossPaymentConfig tossPaymentConfig;
    private final PaymentMapper paymentMapper;
    private final TossConfirmMapper tossConfirmMapper;

    private static final int RESERVATION_EXPIRE_EXTENSION_MINUTES = 3;
    private static final long PAYMENT_APPROVE_TTL_MINUTES = 10L;

    @Transactional
    public PaymentPrepareResponse createPaymentAttempt(Long reservationId) {

        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.TEMP) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 예약 유효시간 연장 (첫 진입 시에만)
        boolean isFirstPaymentAttempt = !paymentAttemptRepository.existsByReservationId(reservationId);
        if (isFirstPaymentAttempt) {
            reservation.extendExpiresAt(RESERVATION_EXPIRE_EXTENSION_MINUTES);
        }

        // 만료된 예약인지 확인
        if(reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.RES_ALREADY_EXPIRED);
        }

        int realAmount = reservation.getTotalAmount();
        PaymentAttempt paymentAttempt = PaymentAttempt.createPending(reservationId, realAmount, PaymentMethod.PG);
        paymentAttemptRepository.save(paymentAttempt);

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        Member member = memberRepository.findById(reservation.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return paymentMapper.toPrepareResponse(
                paymentAttempt,
                room,
                member,
                tossPaymentConfig.getClientKey()
        );
    }

    /**
     * 결제 승인 요청 (분산 락 적용 + 트랜잭션 분리 구조)
     * Flow: [Lock 획득] -> [1차 검증] -> [Toss 호출] -> [DB 저장(Appender)] -> [Lock 해제]
     * 검증: request 는 결제 서비스에서 토스에서 온 response는 저장 컴포넌트에서 처리
     */
    @DistributedLock(
            key = "'payment:approval:' + #request.orderId",
            leaseTime = -1
    )
    public void approveSuccess(String paymentType, PaymentApproveRequest request) {

        // 조회 및 검증 (Toss 호출 전 방어)
        PaymentAttempt attempt = validateAndGetAttempt(request);

        // Toss 결제 승인 요청
        TossConfirmRequest confirmRequest = tossConfirmMapper.toConfirmRequest(request, attempt);
        TossConfirmResponse confirmResponse;

        try {
            confirmResponse = tossPaymentsClient.confirm(confirmRequest);
        } catch (Exception ex) {
            failService.markFailed(request.orderId(), "TOSS_CONFIRM_FAILED", ex.getMessage());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        //  별도 컴포넌트로 위임하여 저장 트랜잭션 수행
        paymentTransactionHelper.appendPayment(attempt, confirmResponse);
    }

    private PaymentAttempt validateAndGetAttempt(PaymentApproveRequest request) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));

        // 시간 검증 (TTL)
        if (attempt.getCreatedAt().plusMinutes(PAYMENT_APPROVE_TTL_MINUTES).isBefore(LocalDateTime.now())) {
            failService.markFailed(request.orderId(), "PAYMENT_EXPIRED", "payment attempt expired");
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        Reservation reservation = reservationRepository.findById(attempt.getReservationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 예약 상태 = TEMP 검증
        if (reservation.getStatus() != ReservationStatus.TEMP) {
            failService.markFailed(request.orderId(), "INVALID_RESERVATION_STATUS", "Reservation is not TEMP");
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 예약이 만료되었는지 검증
        if (reservation.getExpiresAt().isBefore(LocalDateTime.now())) {
            failService.markFailed(request.orderId(), "RESERVATION_EXPIRED", "Reservation time has expired before PG call");
            throw new BusinessException(ErrorCode.RES_ALREADY_EXPIRED);
        }

        // 멱등성 및 상태 검증
        if (attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.SUCCESS) {
            // 이미 성공한 요청이 동일한 키로 들어왔으면 통과 (멱등성)
            if (attempt.getPaymentKey() != null && attempt.getPaymentKey().equals(request.paymentKey())) {
                return attempt;
            }
            throw new BusinessException(ErrorCode.PAYMENT_DUPLICATE_APPROVE);
        }

        if (attempt.getPaymentAttemptStatus() != PaymentAttemptStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }

        if (request.amount() != attempt.getAmount()) {
            failService.markFailed(request.orderId(), "AMOUNT_PARAM_MISMATCH", "amount param mismatch");
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        return attempt;
    }
}