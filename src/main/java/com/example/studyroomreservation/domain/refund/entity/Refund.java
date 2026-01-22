package com.example.studyroomreservation.domain.refund.entity;


import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.global.common.BaseAuditableEntity;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "applied_refund_policy_id")
    private Long appliedRefundPolicyId;

    @Column(nullable = false)
    private Long refund_amount;

    @Enumerated(EnumType.STRING)
    private RefundStatus status;

    private LocalDateTime refundedAt;

    private Refund(Payment payment, Long appliedRefundPolicyId, Long refundAmount) {
        this.payment = payment;
        this.appliedRefundPolicyId = appliedRefundPolicyId;
        this.refund_amount = refundAmount;
        this.status = RefundStatus.PENDING;
        this.refundedAt = null;

    }

    public static Refund create(Payment payment, Long appliedRefundPolicyId, Long refundAmount) {
        if (refundAmount <= 0) {
            //  에러코드 작성 컨벤션 정하기 ex)HttpStatus.BAD_REQUEST,
            //throw new
        }
        return new Refund(payment, appliedRefundPolicyId, refundAmount);
    }
}
