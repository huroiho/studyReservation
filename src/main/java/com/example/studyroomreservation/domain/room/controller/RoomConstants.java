package com.example.studyroomreservation.domain.room.controller;

public final class RoomConstants {

    private RoomConstants() {}

    // ==========================================
    // 1. Room (스터디룸)
    // ==========================================
    
    // --- 사용자 API ---
    public static final String API_ROOM_BASE = "/api/rooms";
    public static final String API_ROOM_SLOTS = "/{roomId}/slots";
    public static final String API_ROOM_POLICY = "/{roomId}/policy";

    // --- 사용자 View ---
    public static final String VIEW_ROOM_BASE = "/rooms";
    public static final String VIEW_ROOM_DETAIL = "/{roomId}";

    public static final String TMPL_ROOM_LIST = "room/user/list";
    public static final String TMPL_ROOM_DETAIL = "room/user/detail";

    // --- 관리자 API ---
    public static final String API_ADMIN_ROOM_BASE = "/api/admin/rooms";

    // --- 관리자 View ---
    public static final String VIEW_ADMIN_ROOM_BASE = "/admin/rooms";
    public static final String VIEW_ADMIN_ROOM_CREATE = "/new";
    public static final String VIEW_ADMIN_ROOM_EDIT = "/{id}/edit";
    public static final String VIEW_ADMIN_ROOM_TOGGLE = "/{id}/toggle";
    public static final String VIEW_ADMIN_ROOM_DELETE = "/{id}/delete";

    public static final String TMPL_ADMIN_ROOM_LIST = "room/admin/list";
    public static final String TMPL_ADMIN_ROOM_CREATE = "room/admin/form";

    public static final String REDIRECT_ADMIN_ROOM_LIST = "redirect:" + VIEW_ADMIN_ROOM_BASE;


    // ==========================================
    // 2. RoomRule (예약 규칙) - 관리자 전용
    // ==========================================
    
    // --- API ---
    public static final String API_ADMIN_ROOM_RULE_BASE = "/api/admin/room-rules";
    public static final String API_ROOM_RULE_PICK = "/pick-items";
    public static final String API_ROOM_RULE_STATUS = "/{id}/status";

    // --- View ---
    public static final String VIEW_ADMIN_ROOM_RULE_BASE = "/admin/room-rules";
    public static final String VIEW_ADMIN_ROOM_RULE_NEW = "/new";
    public static final String VIEW_ADMIN_ROOM_RULE_DETAIL = "/{id}";

    public static final String TMPL_ADMIN_ROOM_RULE_LIST = "room/admin/rule/list";
    public static final String TMPL_ADMIN_ROOM_RULE_FORM = "room/admin/rule/form";
    public static final String TMPL_ADMIN_ROOM_RULE_DETAIL = "room/admin/rule/detail";

    public static final String REDIRECT_ADMIN_ROOM_RULE_LIST = "redirect:" + VIEW_ADMIN_ROOM_RULE_BASE;

    // --- Model Attributes (RoomRule) ---
    public static final String MODEL_ROOMRULES = "roomRules";
    public static final String MODEL_ROOMRULE_REQUEST = "roomRuleRequest";
    public static final String MODEL_PAGING = "paging";
    public static final String MODEL_CURRENT_URL = "currentUrl";


    // ==========================================
    // 3. OperationPolicy (운영 정책) - 관리자 전용
    // ==========================================
    
    // --- API ---
    public static final String API_ADMIN_OP_POLICY_BASE = "/api/admin/operation-policies";
    public static final String API_OP_POLICY_PICK = "/pick-items";
    public static final String API_OP_POLICY_ROOMS = "/{policyId}/rooms";

    // --- View ---
    public static final String VIEW_ADMIN_OP_POLICY_BASE = "/admin/operation-policies";
    public static final String VIEW_ADMIN_OP_POLICY_NEW = "/new";
    public static final String VIEW_ADMIN_OP_POLICY_DETAIL = "/{id}";
    public static final String VIEW_ADMIN_OP_POLICY_ACTIVATE = "/{id}/activate";
    public static final String VIEW_ADMIN_OP_POLICY_DEACTIVATE = "/{id}/deactivate";
    public static final String VIEW_ADMIN_OP_POLICY_DELETE = "/{id}/delete";

    public static final String TMPL_ADMIN_OP_POLICY_LIST = "room/admin/policy/list";
    public static final String TMPL_ADMIN_OP_POLICY_CREATE = "room/admin/policy/form";
    public static final String TMPL_ADMIN_OP_POLICY_DETAIL = "room/admin/policy/detail";

    public static final String REDIRECT_ADMIN_OP_POLICY_LIST = "redirect:" + VIEW_ADMIN_OP_POLICY_BASE;
    public static final String REDIRECT_ADMIN_OP_POLICY_DETAIL = "redirect:" + VIEW_ADMIN_OP_POLICY_BASE + "/";
    
    // --- Model Attributes & Params (OperationPolicy) ---
    public static final String MODEL_OP_FORM = "form";
    public static final String MODEL_OP_POLICY = "policy";
    public static final String MODEL_OP_PAGE = "page";
    public static final String MODEL_OP_SLOT_UNITS = "slotUnits";
    public static final String MODEL_OP_HOURS = "hours";
    public static final String MODEL_OP_DAYS = "days";
    
    public static final String PARAM_OP_SEARCH = "search";
    public static final String PARAM_OP_STATUS = "status";
    public static final String PARAM_OP_REDIRECT = "redirect";
    
    public static final String VAL_OP_STATUS_ACTIVE = "active";
    public static final String VAL_OP_STATUS_INACTIVE = "inactive";
    public static final String VAL_OP_REDIRECT_LIST = "list";
}
