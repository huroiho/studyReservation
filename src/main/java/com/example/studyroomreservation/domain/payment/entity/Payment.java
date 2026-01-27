package com.example.studyroomreservation.domain.payment.entity;

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
@Table(name = "payments")
public class Payment extends BaseAuditableEntity {

    @Column(name = "reservation_id", nullable = false, unique = true)
    private Long reservationId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "pg_tid", unique = true, length = 100)
    private String pgTid;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    private Payment(
            Long reservationId,
            int amount,
            PaymentMethod paymentMethod
    ){
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public static Payment createPending(
            Long reservationId,
            int amount,
            PaymentMethod method
    ) {
        return new Payment(
                reservationId,
                amount,
                method
        );
    }

    public void markAsSuccess(String pgTid, LocalDateTime pgApprovalTime) {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }
        this.pgTid = pgTid;
        this.paymentStatus = PaymentStatus.SUCCESS;
        this.approvedAt = (pgApprovalTime != null) ? pgApprovalTime : LocalDateTime.now();
    }

    public void markAsFailed() {
        if (this.paymentStatus != PaymentStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }
        this.paymentStatus = PaymentStatus.FAILED;
    }
}
