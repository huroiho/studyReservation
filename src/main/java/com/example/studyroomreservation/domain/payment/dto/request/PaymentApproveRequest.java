package com.example.studyroomreservation.domain.payment.dto.request;

import com.example.studyroomreservation.domain.payment.entity.PaymentMethod;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record PaymentApproveRequest(

        @NotNull(message = "예약 ID는 필수입니다.")
        Long reservationId,

        @NotBlank(message = "PG 키값은 필수입니다.")
        String pgTid,

        @Min(value = 1, message = "결제 금액은 0원보다 큰 값이어야 합니다.")
        int amount,

        @NotNull(message = "결제 방법은 필수입니다.")
        PaymentMethod paymentMethod,

        @PastOrPresent(message = "승인 시간은 현재 또는 과거여야 합니다.")
        LocalDateTime approvedAt
) {}
