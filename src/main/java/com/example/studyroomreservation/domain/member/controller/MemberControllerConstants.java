package com.example.studyroomreservation.domain.member.controller;

public final class MemberControllerConstants {
    private MemberControllerConstants() {}

    public static final String BASE = "/my";

    public static final String LOGIN = "/login";
    public static final String SIGNUP = "/signup";
    public static final String EDIT = "/edit";
    public static final String PASSWORD = "/password";

    public static final String REDIRECT_MY = "redirect:" + BASE;
    public static final String REDIRECT_LOGIN = "redirect:" + LOGIN;

    public static final String MEMBER_MYPAGE = "member/my";
    public static final String MEMBER_EDIT = "member/edit";
    public static final String MEMBER_PASSWORD = "member/password";
    public static final String MEMBER_LOGIN = "member/login";
    public static final String MEMBER_SIGNUP = "member/signup";

    // 관리자
    public static final String ADMIN_MEMBER_LIST_VIEW = "/admin/members";
    public static final String ADMIN_MEMBER_LIST = "member/admin/list";
}
