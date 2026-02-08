package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.refund.service.RefundService;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationTransactionHelper {

    private final ReservationRepository reservationRepository;
    private final RefundService refundService;


    @Transactional
    public void completeCancellation(Long reservationId, Long refundPolicyId, long refundAmount) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.cancel(LocalDateTime.now());

        // 환불 이력 저장
        // refundAmount가 0원이어도 기록을 위해 저장
        refundService.createRefund(reservationId, refundPolicyId, refundAmount);
    }
}
