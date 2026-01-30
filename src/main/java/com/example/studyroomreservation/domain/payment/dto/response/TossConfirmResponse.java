package com.example.studyroomreservation.domain.payment.dto.response;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record TossConfirmResponse(

        String orderId,
        String paymentKey,
        int totalAmount,
        String status,
        OffsetDateTime approvedAt
) {
}
