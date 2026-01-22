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
//    @Column(nullable = false)
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





}