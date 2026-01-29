package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

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
    //private final ReservationService reservationService; // TODO : 예약 파트 하면 주석 제거

    public void  approveSuccess(String paymentType,PaymentApproveRequest request){

        String lockKey = "payment:approval:" + request.orderId(); // 페이먼츠 말고 다른곳에서 받을려면 매개변수로 키명 받아와서
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean available = lock.tryLock(5, 10, TimeUnit.SECONDS); // 락을 정의 하는거다
            if (!available) {
                throw new BusinessException(ErrorCode.PAYMENT_CONFLICT); // 예외 코드도 다르게
            }
            transactionTemplate.execute(status -> {
                processingApprovalLogic(paymentType,request); //돌아가는 로직 바껴야하고
                return null;
            });
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
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
