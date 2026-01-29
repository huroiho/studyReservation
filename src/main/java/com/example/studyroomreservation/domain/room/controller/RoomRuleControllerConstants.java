package com.example.studyroomreservation.domain.room.controller;

public final class RoomRuleControllerConstants {
    private RoomRuleControllerConstants(){}

    // === Paths ===
    public static final String BASE_PATH = "/admin/roomrules";
    public static final String NEW_FORM_PATH = "/new";
    public static final String REDIRECT_BASE = "redirect:/admin/roomrules";

    // === View Names ===
    public static final String LIST_VIEW = "room/admin/roomrule-list";
    public static final String DETAIL_VIEW = "room/admin/roomrule-detail";
    public static final String FORM_VIEW = "room/admin/roomrule-form";

    // === Model Attribute Names ===
    public static final String ROOMRULES = "roomRules";
    public static final String ROOMRULE_REQUEST = "roomRuleRequest";
    public static final String PAGING = "paging";
    public static final String CURRENT_URL = "currentUrl";
}