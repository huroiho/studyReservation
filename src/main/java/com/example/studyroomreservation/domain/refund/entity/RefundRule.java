package com.example.studyroomreservation.domain.refund.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
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

    public static RefundRule createRule(String name, Integer refundBaseMinutes, Integer refundRate) {
        if (refundRate < 0 || refundRate > 100) {
            throw new BusinessException(ErrorCode.REF_RATE_INVALID);
        }
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.REF_RULE_NAME_REQUIRED);
        }
        return new RefundRule(name, refundBaseMinutes, refundRate);
    }

    void setRefundPolicy(RefundPolicy refundPolicy) {
        if (this.refundPolicy != null) {
            throw new BusinessException(ErrorCode.REF_POLICY_ALREADY_ASSIGNED);
        }
        this.refundPolicy = refundPolicy;
    }
}