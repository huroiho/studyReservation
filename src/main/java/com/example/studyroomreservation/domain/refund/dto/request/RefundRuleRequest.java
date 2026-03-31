package com.example.studyroomreservation.domain.refund.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefundRuleRequest(
        @NotBlank(message = "정책 이름은 필수입니다.")
        String name,

        @NotNull(message= "기준 시간은 필수입니다.")
        @Max(value = 10080, message = "최대 7일 전까지만 설정 가능합니다.")
        @Min(value = 0, message = "기준 시간은 0분 이상이어야 합니다.")
        Integer refundBaseMinutes,

        @NotNull(message = "환불 비율은 필수입니다.")
        @Min(value = 0, message = "환불 비율은 0% 이상이어야 합니다.")
        @Max(value = 100, message = "환불 비율은 100% 이하여야 합니다.")
        Integer refundRate
) {}
