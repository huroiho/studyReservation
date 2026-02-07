package com.example.studyroomreservation.domain.member.controller;

import com.example.studyroomreservation.domain.member.dto.response.MemberAdminResponse;
import com.example.studyroomreservation.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static com.example.studyroomreservation.domain.member.controller.MemberConstants.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(VIEW_ADMIN_MEMBER_BASE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberController {

    private final MemberService memberService;

    // 회원목록 관리
    @GetMapping
    public String getAllMembers(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ){
        Page<MemberAdminResponse> memberPage = memberService.getMembersForAdmin(keyword,pageable);

        model.addAttribute("page", memberPage);
        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("keyword", keyword);
        return TMPL_ADMIN_MEMBER_LIST;
    }
}
