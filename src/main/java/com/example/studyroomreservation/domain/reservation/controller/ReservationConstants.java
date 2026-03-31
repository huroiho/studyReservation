package com.example.studyroomreservation.domain.reservation.controller;

public final class ReservationConstants {

    private ReservationConstants() {}

    // ==========================================
    // 1. API (사용자)
    // ==========================================
    public static final String API_RESERVATION_BASE = "/api/reservations";
    public static final String API_REFUND_CALCULATION = "/{reservationId}/refund-calculation";
    public static final String API_RESERVATION_CANCEL = "/{reservationId}/cancel";


    // ==========================================
    // 2. View (사용자)
    // ==========================================
    public static final String VIEW_RESERVATION_BASE = "/reservations";
    public static final String VIEW_RESERVATION_DETAIL = "/{reservationId}";
    public static final String VIEW_RESERVATION_CANCEL = "/{reservationId}/cancel";

    // --- MyPage (내 예약 목록) ---
    // 기존 URL 유지: /members/myPage/reservations
    public static final String VIEW_MY_RESERVATION_BASE = "/members/mypage";
    public static final String VIEW_MY_RESERVATION_LIST = "/reservations";
    public static final String VIEW_MY_RESERVATION_HISTORY = "/history";

    // --- Templates ---
    public static final String TMPL_RESERVATION_DETAIL = "reservation/user/mypage-reservation-detail";
    public static final String TMPL_MY_RESERVATION_LIST = "reservation/user/mypage-reservation-list";
    public static final String TMPL_MY_RESERVATION_HISTORY = "reservation/user/mypage-history";

    // --- Redirects ---
    public static final String REDIRECT_RESERVATION_DETAIL = "redirect:" + VIEW_RESERVATION_BASE + "/";


    // ==========================================
    // 3. View (관리자)
    // ==========================================
    public static final String VIEW_ADMIN_RESERVATION_BASE = "/admin/reservations";
    public static final String VIEW_ADMIN_RESERVATION_DETAIL = "/{reservationId}";

    // --- Templates ---
    public static final String TMPL_ADMIN_RESERVATION_LIST = "reservation/admin/list";
    public static final String TMPL_ADMIN_RESERVATION_DETAIL = "reservation/admin/detail";
}
