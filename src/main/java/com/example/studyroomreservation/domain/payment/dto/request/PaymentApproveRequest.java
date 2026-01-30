package com.example.studyroomreservation.domain.payment.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record PaymentApproveRequest(

        @NotBlank(message = "orderId는 필수입니다.")
        String orderId,

        @NotBlank(message = "paymentKey는 필수입니다.")
        String paymentKey,

        @Min(value = 1, message = "결제 금액은 0원보다 큰 값이어야 합니다.")
        int amount
) {}
