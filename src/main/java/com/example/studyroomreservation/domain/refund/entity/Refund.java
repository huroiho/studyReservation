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

    //TODO: 결제에 대해 환불이 1회만 가능하도록 할건지, 다회 환불이 가능하도록 할 건지 -> 수정 완료
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
            throw new IllegalArgumentException("환불 대상 결제 정보는 필수입니다.");
        }

        // 환불 금액 검증 (0원 이하 환불 불가 정책 가정)
        if (refundAmount == null || refundAmount <= 0) {
            throw new IllegalArgumentException("환불 금액은 0원보다 커야 합니다.");
        }

        return new Refund(payment, appliedRefundPolicyId, refundAmount);
    }

    //TODO: 환불시 pg사에서 뭘 주는지 몰라서 추후 인자 값 넣기 -> 없으면 서버시간으로
    public void complete(LocalDateTime pgRefundedAt){
        this.status = RefundStatus.COMPLETED;
        this.refundedAt = (pgRefundedAt != null) ? pgRefundedAt : LocalDateTime.now();
    }

    public void failed(LocalDateTime pgFailedAt){
        this.status = RefundStatus.FAILED;
        this.refundedAt = (pgFailedAt != null) ? pgFailedAt : LocalDateTime.now();
    }
}
