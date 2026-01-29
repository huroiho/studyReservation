package com.example.studyroomreservation.domain.payment.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_attempts")
public class PaymentAttempt extends BaseCreatedEntity {

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentAttemptStatus paymentAttemptStatus;

    @Column(name = "pg_tid", length = 100)
    private String paymentKey;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    private PaymentAttempt(
            Long reservationId,
            String orderId,
            int amount,
            PaymentMethod paymentMethod
    ){
        this.reservationId = reservationId;
        this.amount = amount;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentAttemptStatus = PaymentAttemptStatus.PENDING;
    }

    public static PaymentAttempt createPending(
            Long reservationId,
            String orderId,
            int amount,
            PaymentMethod paymentMethod
    ) {
        return new PaymentAttempt(
                reservationId,
                orderId,
                amount,
                paymentMethod
        );
    }

    public void markAsSuccess(String pgTid) {
        if (this.paymentAttemptStatus == PaymentAttemptStatus.SUCCESS) {
            if (this.paymentKey != null && this.paymentKey.equals(pgTid)) return;
            throw new BusinessException(ErrorCode.PAYMENT_DUPLICATE_APPROVE);
        }

        if (this.paymentAttemptStatus != PaymentAttemptStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }

        this.paymentKey = pgTid;
        this.errorCode = null;
        this.errorMessage = null;
        this.paymentAttemptStatus = PaymentAttemptStatus.SUCCESS;
    }

    public void markFailed(String errorCode, String errorMessage) {
        if (this.paymentAttemptStatus != PaymentAttemptStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }
        this.paymentKey = null;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.paymentAttemptStatus = PaymentAttemptStatus.FAILED;
    }
}
