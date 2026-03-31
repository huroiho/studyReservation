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

    @Column(name = "success_order_id", nullable = false)
    private String successOrderId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "pg_tid", nullable = false, unique = true, length = 100)
    private String paymentKey;

    @Column(name = "approved_at", nullable = false)
    private LocalDateTime approvedAt;

    private Payment(
            Long reservationId,
            String successOrderId,
            int amount,
            PaymentMethod paymentMethod,
            String paymentKey,
            LocalDateTime approvedAt
    ){
        this.reservationId = reservationId;
        this.successOrderId = successOrderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentKey = paymentKey;
        this.approvedAt = (approvedAt != null) ? approvedAt : LocalDateTime.now();
        this.paymentStatus = PaymentStatus.SUCCESS;
    }

    public static Payment createSuccess(
            Long reservationId,
            String orderId,
            int amount,
            PaymentMethod method,
            String paymentKey,
            LocalDateTime approvedAt
    ) {
        return new Payment(
                reservationId,
                orderId,
                amount,
                method,
                paymentKey,
                approvedAt
        );
    }

    public void markRefunded() {
        if (this.paymentStatus != PaymentStatus.SUCCESS) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }
        this.paymentStatus = PaymentStatus.REFUNDED;
    }
}
