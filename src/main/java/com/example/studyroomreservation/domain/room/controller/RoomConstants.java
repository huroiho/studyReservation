package com.example.studyroomreservation.domain.room.controller;

public class RoomConstants {

    // ==========================================
    // API URL (사용자)
    // ==========================================
    public static final String API_ROOM_BASE = "/api/rooms";
    public static final String API_ROOM_SLOTS = "/{roomId}/slots";
    public static final String API_ROOM_POLICY = "/{roomId}/policy";

    // ==========================================
    // API URL (관리자)
    // ==========================================
    public static final String API_ADMIN_ROOM_BASE = "/api/admin/rooms";
    public static final String API_ADMIN_ROOM_RULE_BASE = "/api/admin/room-rules";
    public static final String API_ADMIN_OP_POLICY_BASE = "/api/admin/operation-policies";

    // ==========================================
    // View URL (사용자)
    // ==========================================
    public static final String VIEW_ROOM_BASE = "/rooms";
    public static final String VIEW_ROOM_DETAIL = "/{roomId}";

    // ==========================================
    // View URL (관리자)
    // ==========================================
    public static final String VIEW_ADMIN_ROOM_BASE = "/admin/rooms";
    public static final String VIEW_ADMIN_ROOM_CREATE = "/new";

    public static final String VIEW_ADMIN_ROOM_RULE_BASE = "/admin/room-rules";
    public static final String VIEW_ADMIN_ROOM_RULE_CREATE = "/new";
    public static final String VIEW_ADMIN_ROOM_RULE_DETAIL = "/{ruleId}";

    public static final String VIEW_ADMIN_OP_POLICY_BASE = "/admin/operation-policies";
    public static final String VIEW_ADMIN_OP_POLICY_CREATE = "/new";
    public static final String VIEW_ADMIN_OP_POLICY_DETAIL = "/{policyId}";

    // ==========================================
    // Template Path (View 파일 경로)
    // ==========================================
    // 사용자
    public static final String TMPL_ROOM_LIST = "room/user/list";
    public static final String TMPL_ROOM_DETAIL = "room/user/detail";

    // 관리자 - 룸
    public static final String TMPL_ADMIN_ROOM_LIST = "room/admin/room-list";
    public static final String TMPL_ADMIN_ROOM_CREATE = "room/admin/create";

    // 관리자 - 예약 규칙
    public static final String TMPL_ADMIN_ROOM_RULE_LIST = "room/admin/roomrule-list";
    public static final String TMPL_ADMIN_ROOM_RULE_FORM = "room/admin/roomrule-form";
    public static final String TMPL_ADMIN_ROOM_RULE_DETAIL = "room/admin/roomrule-detail";

    // 관리자 - 운영 정책
    public static final String TMPL_ADMIN_OP_POLICY_LIST = "room/operation-policy/list";
    public static final String TMPL_ADMIN_OP_POLICY_CREATE = "room/operation-policy/create";
    public static final String TMPL_ADMIN_OP_POLICY_DETAIL = "room/operation-policy/detail";

    // ==========================================
    // Redirect
    // ==========================================
    public static final String REDIRECT_ADMIN_ROOM_LIST = "redirect:" + VIEW_ADMIN_ROOM_BASE;
    public static final String REDIRECT_ADMIN_ROOM_RULE_LIST = "redirect:" + VIEW_ADMIN_ROOM_RULE_BASE;
    public static final String REDIRECT_ADMIN_OP_POLICY_LIST = "redirect:" + VIEW_ADMIN_OP_POLICY_BASE;
}
