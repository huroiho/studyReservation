package com.example.studyroomreservation.domain.payment.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "payment_attempts")
public class PaymentAttempt extends BaseCreatedEntity {

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentAttemptStatus paymentAttemptStatus;

    @Column(name = "pg_tid", length = 100)
    private String pgTid;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    private PaymentAttempt(
            Long reservationId,
            int amount,
            PaymentMethod paymentMethod
    ){
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentAttemptStatus = PaymentAttemptStatus.PENDING;
    }

    public static PaymentAttempt createPending(
            Long reservationId,
            int amount,
            PaymentMethod paymentMethod
    ) {
        return new PaymentAttempt(
                reservationId,
                amount,
                paymentMethod
        );
    }

    public void markAsSuccess(String pgTid) {
        if (this.paymentAttemptStatus != PaymentAttemptStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }
        this.pgTid = pgTid;
        this.errorCode = null;
        this.errorMessage = null;
        this.paymentAttemptStatus = PaymentAttemptStatus.SUCCESS;
    }

    public void markFailed(String errorCode, String errorMessage) {
        if (this.paymentAttemptStatus != PaymentAttemptStatus.PENDING) {
            throw new BusinessException(ErrorCode.PAYMENT_STATUS_INVALID_TRANSITION);
        }
        this.pgTid = null;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.paymentAttemptStatus = PaymentAttemptStatus.FAILED;
    }
}
