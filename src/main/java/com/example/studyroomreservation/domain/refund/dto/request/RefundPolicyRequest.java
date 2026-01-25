package com.example.studyroomreservation.domain.refund.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RefundPolicyRequest(
        @NotBlank(message = "정책 이름은 필수입니다.")
        String name,
        @NotEmpty(message = "최소 하나 이상의 규칙이 필요합니다.")
        @Valid
        List<RefundRuleRequest> rules
) {}
