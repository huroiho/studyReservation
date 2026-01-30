package com.example.studyroomreservation.domain.room.controller;

public final class OperationPolicyControllerConstants {
    private OperationPolicyControllerConstants() {}

    // ===== API Controller =====
    public static final String API_BASE_PATH = "/api/operation-policies";
    public static final String ROOMS_BY_POLICY = "/{policyId}/rooms";


    // ===== MVC Controller =====
    // --- URL Paths ---
    // 관리자 운영정책 기본 경로 (RequestMapping에 사용)
    public static final String BASE_PATH = "/admin/operation-policies";

    // 하위 경로 (GetMapping/PostMapping에 사용)
    public static final String NEW_FORM_PATH = "/new";
    public static final String DETAIL_PATH = "/{id}";
    public static final String ACTIVATE_PATH = "/{id}/activate";
    public static final String DEACTIVATE_PATH = "/{id}/deactivate";
    public static final String DELETE_PATH = "/{id}/delete";

    // 리다이렉트 경로
    public static final String REDIRECT_BASE = "redirect:" + BASE_PATH + "/";
    public static final String REDIRECT_LIST = "redirect:" + BASE_PATH;

    public static final String PARAM_REDIRECT = "redirect";
    public static final String REDIRECT_TARGET_LIST = "list";

    // === View Names ===
    // Thymeleaf 템플릿 경로 (templates/ 하위)
    private static final String VIEW_PREFIX = "room/operation-policy/";
    public static final String CREATE_VIEW = VIEW_PREFIX + "create";
    public static final String DETAIL_VIEW = VIEW_PREFIX + "detail";
    public static final String LIST_VIEW = VIEW_PREFIX + "list";

    // === Model Attribute Names ===
    // 뷰에서 사용하는 모델 속성명
    public static final String FORM = "form";
    public static final String POLICY = "policy";
    public static final String PAGE = "page";
    public static final String SLOT_UNITS = "slotUnits";
    public static final String HOURS = "hours";
    public static final String DAYS = "days";

    // === Request Parameter Names ===
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_STATUS = "status";

    // === Status Filter Values ===
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_INACTIVE = "inactive";
}
