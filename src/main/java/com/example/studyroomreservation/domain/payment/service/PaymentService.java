package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.entity.PaymentStatus;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    //private final ReservationService reservationService; // TODO : 예약 파트 하면 주석 제거

    @Transactional
    public void approveSuccess(PaymentApproveRequest request) {
        Long reservationId = request.reservationId();

        paymentRepository.findByReservationId(reservationId).ifPresent(existing -> {
            if (existing.getPaymentStatus() == PaymentStatus.SUCCESS) {
                if (existing.getPgTid() != null && existing.getPgTid().equals(request.pgTid())) {
                    return;
                }
                throw new BusinessException(ErrorCode.PAYMENT_DUPLICATE_APPROVE);
            }
        });

        PaymentAttempt attempt = paymentAttemptRepository.findLatestPendingByReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));

        if (attempt.getAmount() != request.amount()) {
            attempt.markFailed("AMOUNT_MISMATCH", "PG amount mismatch");
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        Payment payment = paymentRepository.findByReservationId(reservationId) // FIXME : 동시성 문제 생각하고 , 락 , 레파짓 PGTID 로 조회하는방법찾기
                .orElseGet(() -> paymentRepository.save(
                        Payment.createPending(
                                reservationId,
                                request.amount(),
                                request.paymentMethod()
                        )
                ));

        attempt.markAsSuccess(request.pgTid());
        payment.markAsSuccess(request.pgTid(), request.approvedAt());

        //reservationService.updateReservationStatus(reservationId, ReservationStatus.PAID); // TODO : 예약 파트 하면 주석 제거
    }
}
