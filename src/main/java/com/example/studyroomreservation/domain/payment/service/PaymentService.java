package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.entity.PaymentMethod;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.config.TossPaymentConfig;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import com.example.studyroomreservation.domain.payment.client.TossPaymentsClient;
import com.example.studyroomreservation.domain.payment.dto.request.TossConfirmRequest;
import com.example.studyroomreservation.domain.payment.dto.response.TossConfirmResponse;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttemptStatus;
import com.example.studyroomreservation.domain.payment.mapper.PaymentMapper;
import com.example.studyroomreservation.domain.payment.mapper.TossConfirmMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final TransactionTemplate transactionTemplate;
    private final TossConfirmMapper tossConfirmMapper;
    private final PaymentMapper paymentMapper;
    private final RedissonClient redissonClient;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentAttemptFailService failService;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final TossPaymentConfig tossPaymentConfig;
    //private final ReservationService reservationService; // TODO : 예약 파트 하면 주석 제거
    private static final int RESERVATION_EXPIRE_EXTENSION_MINUTES = 3;

    @Transactional
    public PaymentPrepareResponse createPaymentAttempt(Long reservationId) {

        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.TEMP) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        boolean isFirstPaymentAttempt = !paymentAttemptRepository.existsByReservationId(reservationId);

        if (isFirstPaymentAttempt) {
            reservation.extendExpiresAt(RESERVATION_EXPIRE_EXTENSION_MINUTES);
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

    public void approveSuccess(String paymentType, PaymentApproveRequest request) {
        String lockKey = "payment:approval:" + request.orderId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new BusinessException(ErrorCode.PAYMENT_IN_PROGRESS);
            }

            transactionTemplate.execute(status -> {
                processingApprovalLogic(paymentType, request);
                return null;
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);

        } finally {
            if (locked) {
                try {
                    lock.unlock();
                } catch (Exception ignore) {
                }
            }
        }
    }

    //@Transactional
    public void processingApprovalLogic(String paymentType, PaymentApproveRequest request) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));

        Long reservationId = attempt.getReservationId();

        //멱등성
        if (attempt.getPaymentAttemptStatus() == PaymentAttemptStatus.SUCCESS) {

            if (attempt.getPaymentKey() != null && attempt.getPaymentKey().equals(request.paymentKey())) {
                return;
            }
            throw new BusinessException(ErrorCode.PAYMENT_DUPLICATE_APPROVE);
        }

        if (attempt.getPaymentAttemptStatus() != PaymentAttemptStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }

        // successUrl 파라미터 amount 검증
        if (request.amount() != attempt.getAmount()) {
            failService.markFailed(request.orderId(), "AMOUNT_PARAM_MISMATCH", "amount param mismatch");
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 토스 confirm 요청 생성
        TossConfirmRequest confirmRequest = tossConfirmMapper.toConfirmRequest(request, attempt);

        // 토스 confirm 호출
        TossConfirmResponse confirmResponse;
        try {
            confirmResponse = tossPaymentsClient.confirm(confirmRequest);
        } catch (Exception ex) {
            failService.markFailed(request.orderId(), "TOSS_CONFIRM_FAILED", "toss confirm failed");
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }

        // 최종 금액 검증
        if (confirmResponse.totalAmount() != attempt.getAmount()) {
            failService.markFailed(request.orderId(), "AMOUNT_MISMATCH", "PG amount mismatch");
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        if (!confirmResponse.paymentKey().equals(request.paymentKey())) {
            failService.markFailed(request.orderId(), "PAYMENT_KEY_MISMATCH", "paymentKey mismatch");
            throw new BusinessException(ErrorCode.PAYMENT_INVALID_REQUEST);
        }

        attempt.markAsSuccess(confirmResponse.paymentKey());
        if (paymentRepository.existsByReservationId(reservationId)) {
            return;
        }
        try {
            Payment payment = paymentMapper.toPaymentSuccess(attempt, confirmResponse);
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {

            return;
        }

        //reservationService.updateReservationStatus(reservationId, ReservationStatus.PAID); // TODO : 예약 도메인 생성시 주석 제거
    }
}
