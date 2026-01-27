package com.example.studyroomreservation.domain.payment.dto.response;

public record PaymentPrepareResponse (
        String orderId,
        String orderName,
        long amount,
        String customerEmail,
        String customerName,
        String customerMobilePhone

){}
