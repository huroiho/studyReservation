package com.example.studyroomreservation.domain.refund.service;

import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.refund.entity.Refund;
import com.example.studyroomreservation.domain.refund.repository.RefundRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public Long createRefund(Long reservationId, Long appliedRefundPolicyId, long refundAmount) {
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Refund refund = Refund.create(payment, appliedRefundPolicyId, refundAmount);
        
        // PG사 환불 성공 시각 기록 (현재는 즉시 성공 가정)
        refund.complete(LocalDateTime.now());
        
        refundRepository.save(refund);
        return refund.getId();
    }
}
