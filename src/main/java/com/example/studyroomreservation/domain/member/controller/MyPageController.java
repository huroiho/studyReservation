package com.example.studyroomreservation.domain.member.controller;

import com.example.studyroomreservation.domain.member.dto.request.MemberPasswordChangeRequest;
import com.example.studyroomreservation.domain.member.dto.request.MemberUpdateRequest;
import com.example.studyroomreservation.domain.member.dto.response.MemberInfoResponse;
import com.example.studyroomreservation.domain.member.service.MemberService;
import com.example.studyroomreservation.domain.member.validation.MemberPasswordChangeValidator;
import com.example.studyroomreservation.domain.member.validation.MemberUpdateValidator;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my")
public class MyPageController {

    private final MemberService memberService;
    private final MemberUpdateValidator memberUpdateValidator;
    private final MemberPasswordChangeValidator memberPasswordChangeValidator;

    @InitBinder("memberUpdateRequest")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(memberUpdateValidator);
    }

    @InitBinder("memberPasswordChangeRequest")
    public void initPasswordBinder(WebDataBinder binder) {
        binder.addValidators(memberPasswordChangeValidator);
    }

    @GetMapping
    public String myPage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Long memberId = userDetails.getMember().getId();

        model.addAttribute("myInfo", memberService.getMyInfo(memberId));
        return "member/my";
    }

    @GetMapping("/edit")
    public String editForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Long memberId = userDetails.getMember().getId();

        MemberInfoResponse myInfo = memberService.getMyInfo(memberId);

        model.addAttribute("memberUpdateRequest", new MemberUpdateRequest(myInfo.name(), myInfo.phoneNumber()));
        return "member/edit";
    }

    @PostMapping("/edit")
    public String editSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("memberUpdateRequest") MemberUpdateRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "member/edit";
        }

        Long memberId = userDetails.getMember().getId();
        memberService.updateMyProfile(memberId, request);

        return "redirect:/my";
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute(
                "memberPasswordChangeRequest",
                new MemberPasswordChangeRequest("", "", ""));
        return "member/password";
    }

    @PostMapping("/password")
    public String passwordSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("memberPasswordChangeRequest")
            MemberPasswordChangeRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "member/password";
        }
        memberService.changeMyPassword(
                userDetails.getMember().getId(),
                request
        );
        return "redirect:/my";
    }
}
