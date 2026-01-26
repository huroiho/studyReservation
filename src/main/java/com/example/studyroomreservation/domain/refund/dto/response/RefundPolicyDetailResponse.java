package com.example.studyroomreservation.domain.refund.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RefundPolicyDetailResponse(

        Long id,
        String name,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime activeUpdatedAt,
        List<RefundRuleResponse> rules
) {
}
