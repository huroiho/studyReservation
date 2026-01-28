package com.example.studyroomreservation.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaymentPrepareRequest (
        @NotNull(message = "예약 ID는 필수 입니다")
        Long reservationId,
        @Min(value = 100, message = "최소 결제 금액은 100원입니다")
        int amount
){}
