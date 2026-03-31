package com.example.studyroomreservation.domain.member.controller;

import com.example.studyroomreservation.domain.member.dto.request.MemberPasswordChangeRequest;
import com.example.studyroomreservation.domain.member.dto.request.MemberUpdateRequest;
import com.example.studyroomreservation.domain.member.dto.response.MemberInfoResponse;
import com.example.studyroomreservation.domain.member.service.MemberQueryService;
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

import static com.example.studyroomreservation.domain.member.controller.MemberConstants.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(VIEW_MEMBER_BASE)
public class MyPageController {

    private final MemberService memberService;
    private final MemberQueryService memberQueryService;
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

        model.addAttribute("myInfo", memberQueryService.getMyInfo(memberId));
        return TMPL_MEMBER_MYPAGE;
    }

    @GetMapping(VIEW_MEMBER_EDIT)
    public String editForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Long memberId = userDetails.getMember().getId();

        MemberInfoResponse myInfo = memberQueryService.getMyInfo(memberId);

        model.addAttribute("memberUpdateRequest", new MemberUpdateRequest(myInfo.name(), myInfo.phoneNumber()));
        return TMPL_MEMBER_EDIT;
    }

    @PostMapping(VIEW_MEMBER_EDIT)
    public String editSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("memberUpdateRequest") MemberUpdateRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return TMPL_MEMBER_EDIT;
        }

        Long memberId = userDetails.getMember().getId();
        memberService.updateMyProfile(memberId, request);

        return REDIRECT_MEMBER_MY;
    }

    @GetMapping(VIEW_MEMBER_PASSWORD)
    public String passwordForm(Model model) {
        model.addAttribute(
                "memberPasswordChangeRequest",
                new MemberPasswordChangeRequest("", "", ""));
        return TMPL_MEMBER_PASSWORD;
    }

    @PostMapping(VIEW_MEMBER_PASSWORD)
    public String passwordSubmit(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute("memberPasswordChangeRequest")
            MemberPasswordChangeRequest request,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return TMPL_MEMBER_PASSWORD;
        }
        memberService.changeMyPassword(
                userDetails.getMember().getId(),
                request
        );
        return REDIRECT_MEMBER_MY;
    }
}
