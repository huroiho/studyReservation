package com.example.studyroomreservation.domain.payment.dto.request;

public record TossCancelRequest(
        String cancelReason,
        long cancelAmount
) {
}
