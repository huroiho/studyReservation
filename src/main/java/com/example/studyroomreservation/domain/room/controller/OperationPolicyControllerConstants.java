package com.example.studyroomreservation.domain.room.controller;

public final class OperationPolicyControllerConstants {
    private OperationPolicyControllerConstants(){}

    // === Paths ===
    public static final String BASE_PATH = "/admin/operation-policies";
    public static final String NEW_FORM_PATH = "/new";
    public static final String REDIRECT_BASE = "redirect:/admin/operation-policies/";

    // === View Names ===
    public static final String CREATE_VIEW = "room/operation-policy/create";

    // === Model Attribute Names ===
    public static final String FORM = "form";
    public static final String SLOT_UNITS = "slotUnits";
    public static final String HOURS = "hours";
    public static final String DAYS = "days";
}
