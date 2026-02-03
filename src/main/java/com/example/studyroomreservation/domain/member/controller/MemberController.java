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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberSignupValidator memberSignupValidator;

    @InitBinder("memberSignupRequest")
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(memberSignupValidator);
    }

    @GetMapping(MemberControllerConstants.LOGIN)
    public String loginPage() {
        return MemberControllerConstants.VIEW_LOGIN;
    }

    @GetMapping(MemberControllerConstants.SIGNUP)
    public String signupPage(Model model) {
        model.addAttribute("memberSignupRequest", new MemberSignupRequest("", "", "", ""));
        return MemberControllerConstants.VIEW_SIGNUP;
    }

    @PostMapping(MemberControllerConstants.SIGNUP)
    public String signupSubmit(
            @Valid @ModelAttribute("memberSignupRequest")
            MemberSignupRequest memberSignupRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return MemberControllerConstants.VIEW_SIGNUP;
        }
        memberService.signup(memberSignupRequest);
        return  MemberControllerConstants.REDIRECT_LOGIN;
    }
}