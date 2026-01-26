package com.example.studyroomreservation.domain.refund.dto.response;

import java.time.LocalDateTime;

public record RefundPolicyListResponse(

        Long id,
        String name,
        boolean isActive,
        long ruleCount,
        LocalDateTime createdAt,
        LocalDateTime activeUpdatedAt
) {
}