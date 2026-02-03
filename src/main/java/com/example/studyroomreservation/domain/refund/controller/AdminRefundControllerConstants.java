package com.example.studyroomreservation.domain.refund.controller;

public final class AdminRefundControllerConstants {

    private AdminRefundControllerConstants() {}

    public static final String BASE = "/admin/refund";

    public static final String POLICY = "/policy";
    public static final String NEW = "/new";
    public static final String LIST = "/list";

    public static final String POLICY_NEW = POLICY + NEW;
    public static final String POLICY_LIST = POLICY + LIST;
    public static final String POLICY_DETAIL = POLICY + "/{policyId}";
    public static final String POLICY_ACTIVE = POLICY + "/{policyId}/active";

    public static final String REFUND_POLICY_FORM = "refund/admin/policy-form";
    public static final String REFUND_POLICY_LIST = "refund/admin/list";
    public static final String REFUND_POLICY_DETAIL = "refund/admin/detail";

    public static final String REDIRECT_REFUND_POLICY_LIST = "redirect:" + BASE + POLICY_LIST;

    public static String redirectRefundPolicyDetail(Long policyId) {
        return "redirect:" + BASE + POLICY + "/" + policyId;
    }
}
