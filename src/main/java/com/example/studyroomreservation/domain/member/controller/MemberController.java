package com.example.studyroomreservation.domain.member.controller;

import com.example.studyroomreservation.domain.member.dto.request.MemberSignupRequest;
import com.example.studyroomreservation.domain.member.service.MemberService;
import com.example.studyroomreservation.domain.member.validation.MemberSignupValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import static com.example.studyroomreservation.domain.member.controller.MemberConstants.*;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberSignupValidator memberSignupValidator;

    @InitBinder("memberSignupRequest")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(memberSignupValidator);
    }

    @GetMapping(VIEW_MEMBER_LOGIN)
    public String loginPage() {
        return TMPL_MEMBER_LOGIN;
    }

    @GetMapping(VIEW_MEMBER_SIGNUP)
    public String signupPage(Model model) {
        model.addAttribute("memberSignupRequest", new MemberSignupRequest("", "", "", ""));
        return TMPL_MEMBER_SIGNUP;
    }

    @PostMapping(VIEW_MEMBER_SIGNUP)
    public String signupSubmit(
            @Valid @ModelAttribute("memberSignupRequest")
            MemberSignupRequest memberSignupRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return TMPL_MEMBER_SIGNUP;
        }
        memberService.signup(memberSignupRequest);
        return REDIRECT_MEMBER_LOGIN;
    }
}
