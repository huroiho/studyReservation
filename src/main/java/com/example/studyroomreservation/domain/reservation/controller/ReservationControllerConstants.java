package com.example.studyroomreservation.domain.reservation.controller;

public class ReservationControllerConstants {

    private ReservationControllerConstants() {}

    // ==========================================
    // 1. API Controller
    // ==========================================
    public static final String RES_API_BASE_PATH = "/api/reservations";


    // ==========================================
    // 2. Admin MVC Controller
    // ==========================================
    // --- Paths ---
    public static final String ADMIN_BASE_PATH = "/admin/reservations";
    public static final String ADMIN_DETAIL_PATH = "/{reservationId}";

    // --- Views ---
    public static final String RES_ADMIN_LIST = "reservation/admin/adminReservationList";
    public static final String RES_ADMIN_DETAIL = "reservation/admin/detail";


    // ==========================================
    // 3. User (Public) MVC Controller
    // ==========================================
    // --- Paths ---
    public static final String RES_MVC_BASE_PATH = "/reservations";
    public static final String RES_MVC_DETAIL_PATH = "/{reservationId}";
    public static final String RES_MVC_CANCEL_PATH = "/{reservationId}/cancel";

    // --- Views ---
    public static final String RES_USER_DETAIL = "reservation/reservation-detail";

    // --- Redirects ---
    public static final String RES_REDIRECT_RES_DETAIL = "redirect:" + RES_MVC_BASE_PATH + "/";


    // ==========================================
    // 4. MyPage MVC Controller
    // ==========================================
    // --- Paths ---
    public static final String MY_BASE_PATH = "/members/myPage";
    public static final String MY_RESERVATIONS_PATH = "/reservations";

    // --- Views ---
    public static final String MY_RES_LIST = "reservation/mypage-reservation-list";





}