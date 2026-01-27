package com.example.studyroomreservation.domain.refund.dto.response;

import java.time.LocalDateTime;

public record RefundRuleResponse(

        Long id,
        String name,
        Integer refundBaseMinutes,
        Integer refundRate,
        LocalDateTime createdAt
) {
}
