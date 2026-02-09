package com.example.studyroomreservation.domain.refund.entity;


import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.global.common.BaseAuditableEntity;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refunds")
public class Refund extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(name = "applied_refund_policy_id")
    private Long appliedRefundPolicyId;

    @Column(nullable = false)
    private Long refundAmount;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private LocalDateTime refundedAt;

    private Refund(Payment payment, Long appliedRefundPolicyId, Long refundAmount) {
        this.payment = payment;
        this.appliedRefundPolicyId = appliedRefundPolicyId;
        this.refundAmount = refundAmount;
        this.status = RefundStatus.PENDING;
        this.refundedAt = null;

    }

    public static Refund create(Payment payment, Long appliedRefundPolicyId, Long refundAmount) {

        // 결제 정보 검증
        if (payment == null) {
            throw new BusinessException(ErrorCode.REF_PAYMENT_REQUIRED);
        }

        if (refundAmount == null || refundAmount < 0) {
            throw new BusinessException(ErrorCode.REF_AMOUNT_INVALID);
        }

        return new Refund(payment, appliedRefundPolicyId, refundAmount);
    }

    //TODO: 환불시 pg사에서 뭘 주는지 몰라서 추후 인자 값 넣기 -> 없으면 서버시간으로
    public void complete(LocalDateTime pgRefundedTime){
        this.status = RefundStatus.SUCCESS;
        this.refundedAt = (pgRefundedTime != null) ? pgRefundedTime : LocalDateTime.now();
    }

    public void failed(LocalDateTime pgFailedAt){
        this.status = RefundStatus.FAILED;
        this.refundedAt = (pgFailedAt != null) ? pgFailedAt : LocalDateTime.now();
    }
}
