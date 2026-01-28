package com.example.studyroomreservation.domain.payment.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
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
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentAttemptStatus paymentAttemptStatus;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

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
        this.paymentAttemptStatus = PaymentAttemptStatus.PENDING;
        this.orderId = "RES_" + reservationId + "_" + java.util.UUID.randomUUID().toString().substring(0, 5);
    }

    // 팩토리 메서드 수정
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
            throw new IllegalStateException("PENDING 상태에서만 SUCCESS로 변경할 수 있습니다.");
        }
        this.pgTid = pgTid;
        this.errorCode = null;
        this.errorMessage = null;
        this.paymentAttemptStatus = PaymentAttemptStatus.SUCCESS;
    }

    public void markFailed(String errorCode, String errorMessage) {
        if (this.paymentAttemptStatus != PaymentAttemptStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태에서만 FAILED로 변경할 수 있습니다.");
        }
        this.pgTid = null;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.paymentAttemptStatus = PaymentAttemptStatus.FAILED;
    }
}
