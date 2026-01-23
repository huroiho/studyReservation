package com.example.studyroomreservation.domain.refund.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refund_rules")
public class RefundRule extends BaseCreatedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_policy_id", nullable = false)
    private RefundPolicy refundPolicy;

    @Column(nullable = false, length = 50)
    private String name;

    // 추후 개발 요소를 위한 필드
//    @Column(nullable = false
//    private Integer priority;

    @Column(nullable = false)
    private Integer refundBaseMinutes;

    @Column(nullable = false)
    private Integer refundRate;

    private RefundRule(String name, Integer refundBaseMinutes, Integer refundRate) {
        this.name = name;
        this.refundBaseMinutes = refundBaseMinutes;
        this.refundRate = refundRate;
    }

    public static RefundRule create(String name, Integer refundBaseMinutes, Integer refundRate) {
        if (refundRate < 0 || refundRate > 100) {
            throw new IllegalArgumentException("환불 비율은 0~100 사이여야 합니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("규칙 이름은 필수입니다.");
        }
        return new RefundRule(name, refundBaseMinutes, refundRate);
    }

    void setRefundPolicy(RefundPolicy refundPolicy) {
        if (this.refundPolicy != null) {
            throw new IllegalStateException("이미 환불 정책이 할당되어 있습니다.");
        }
        this.refundPolicy = refundPolicy;
    }
}