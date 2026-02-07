package com.example.studyroomreservation.domain.refund.controller;

public final class RefundConstants {

    private RefundConstants() {}

    // ==========================================
    // 1. API (관리자)
    // ==========================================
    public static final String API_ADMIN_REFUND_BASE = "/api/admin/refund-policies";
    public static final String API_REFUND_PICK = "/pick-items";
    public static final String API_REFUND_DETAIL = "/{id}";

    // ==========================================
    // 2. View (관리자)
    // ==========================================
    public static final String VIEW_ADMIN_REFUND_BASE = "/admin/refund";
    public static final String VIEW_ADMIN_REFUND_POLICY = "/policy";
    public static final String VIEW_ADMIN_REFUND_NEW = "/new";
    public static final String VIEW_ADMIN_REFUND_LIST = "/list";

    public static final String VIEW_ADMIN_REFUND_POLICY_NEW = VIEW_ADMIN_REFUND_POLICY + VIEW_ADMIN_REFUND_NEW;
    public static final String VIEW_ADMIN_REFUND_POLICY_LIST = VIEW_ADMIN_REFUND_POLICY + VIEW_ADMIN_REFUND_LIST;
    public static final String VIEW_ADMIN_REFUND_POLICY_DETAIL = VIEW_ADMIN_REFUND_POLICY + "/{policyId}";
    public static final String VIEW_ADMIN_REFUND_POLICY_ACTIVE = VIEW_ADMIN_REFUND_POLICY + "/{policyId}/active";

    // --- Templates ---
    public static final String TMPL_ADMIN_REFUND_FORM = "refund/admin/policy-form";
    public static final String TMPL_ADMIN_REFUND_LIST = "refund/admin/list";
    public static final String TMPL_ADMIN_REFUND_DETAIL = "refund/admin/detail";

    // --- Redirects ---
    public static final String REDIRECT_ADMIN_REFUND_LIST = "redirect:" + VIEW_ADMIN_REFUND_BASE + VIEW_ADMIN_REFUND_POLICY_LIST;

    public static String redirectRefundPolicyDetail(Long policyId) {
        return "redirect:" + VIEW_ADMIN_REFUND_BASE + VIEW_ADMIN_REFUND_POLICY + "/" + policyId;
    }
}
