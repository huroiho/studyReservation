package com.example.studyroomreservation.domain.member.controller;

public final class MemberConstants {
    private MemberConstants() {}

    // ==========================================
    // 1. Member (회원) - 사용자
    // ==========================================
    public static final String VIEW_MEMBER_BASE = "/my";
    public static final String VIEW_MEMBER_LOGIN = "/login";
    public static final String VIEW_MEMBER_SIGNUP = "/signup";
    public static final String VIEW_MEMBER_EDIT = "/edit";
    public static final String VIEW_MEMBER_PASSWORD = "/password";

    public static final String TMPL_MEMBER_LOGIN = "member/user/login";
    public static final String TMPL_MEMBER_SIGNUP = "member/user/signup";
    public static final String TMPL_MEMBER_MYPAGE = "member/user/my";
    public static final String TMPL_MEMBER_EDIT = "member/user/edit";
    public static final String TMPL_MEMBER_PASSWORD = "member/user/password";

    public static final String REDIRECT_MEMBER_MY = "redirect:" + VIEW_MEMBER_BASE;
    public static final String REDIRECT_MEMBER_LOGIN = "redirect:" + VIEW_MEMBER_LOGIN;


    // ==========================================
    // 2. Member (회원) - 관리자
    // ==========================================
    public static final String VIEW_ADMIN_MEMBER_BASE = "/admin/members";

    public static final String TMPL_ADMIN_MEMBER_LIST = "member/admin/list";
}
