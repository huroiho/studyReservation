package com.example.studyroomreservation.domain.payment.controller;

public final class PaymentControllerConstants {
    private PaymentControllerConstants() {}

    public static final String BASE = "/payments";

    public static final String CHECK = "/check";
    public static final String APPROVE = "/approve";
    public static final String FAIL = "/fail";

    public static final String REDIRECT_RESERVATIONS = "redirect:/reservations";

    public static final String PAYMENT_CHECK = "payment/check";
    public static final String PAYMENT_FAIL = "payment/fail";
}
