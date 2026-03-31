package com.example.studyroomreservation.domain.refund.dto.response;

import java.time.LocalDateTime;

public record RefundPolicyListResponse(

        Long id,
        String name,
        boolean active,
        long ruleCount,
        LocalDateTime createdAt,
        LocalDateTime activeUpdatedAt
) {
}