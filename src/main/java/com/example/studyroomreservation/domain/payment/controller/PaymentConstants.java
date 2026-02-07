package com.example.studyroomreservation.domain.payment.controller;

public final class PaymentConstants {
    private PaymentConstants() {}

    // ==========================================
    // 1. View (사용자)
    // ==========================================
    public static final String VIEW_PAYMENT_BASE = "/payments";
    public static final String VIEW_PAYMENT_CHECK = "/check";
    public static final String VIEW_PAYMENT_APPROVE = "/approve";
    public static final String VIEW_PAYMENT_FAIL = "/fail";

    public static final String TMPL_PAYMENT_CHECK = "payment/user/check";
    public static final String TMPL_PAYMENT_FAIL = "payment/user/fail"; // fail.html이 있다면

    // 예약 상세 페이지로 리다이렉트
    public static String redirectReservationDetail(Long reservationId) {
        return "redirect:/reservations/" + reservationId;
    }
    
    // 예약 목록으로 리다이렉트 (ID를 찾을 수 없을 때)
    public static final String REDIRECT_RESERVATION_LIST = "redirect:/members/mypage/reservations";
}
