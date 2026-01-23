package com.example.studyroomreservation.domain.refund.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refund_rule")
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

    private RefundRule(RefundPolicy refundPolicy ,String name, Integer refundBaseMinutes, Integer refundRate ){
        this.refundPolicy = refundPolicy;
        this.name = name;
        this.refundBaseMinutes = refundBaseMinutes;
        this.refundRate = refundRate;
    }

    public static RefundRule create(RefundPolicy refundPolicy, String name, Integer refundBaseMinutes, Integer refundRate) {
        if (refundPolicy == null) {
            throw new IllegalArgumentException("환불 정책(부모) 정보가 없습니다.");
        }
        if (refundRate < 0 || refundRate > 100) {
            throw new IllegalArgumentException("환불 비율은 0~100 사이여야 합니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("규칙 이름은 필수입니다.");
        }

        return new RefundRule(refundPolicy, name, refundBaseMinutes, refundRate);
    }
}