package com.example.studyroomreservation.domain.refund.controller;


import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Slf4j
@Controller
@RequestMapping("/admin/refund")
@RequiredArgsConstructor
public class AdminRefundViewController {

    private final RefundPolicyService refundPolicyService;

    @GetMapping("/policy/new")
    public String createForm(Model model) {
        // 폼 초기화: rules 리스트를 비어있는 상태로라도 넘겨줘야 함
        model.addAttribute("policyForm", new RefundPolicyRequest(null, new ArrayList<>()));
        return "refund/admin/policy-form";
    }

    @PostMapping("policy/new")
    public String register(
            @Valid @ModelAttribute("policyForm") RefundPolicyRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            log.info("validation errors={}", bindingResult);
            return "refund/admin/policy-form";
        }

        Long savedId = refundPolicyService.registerPolicy(request);

        redirectAttributes.addFlashAttribute("message", "환불 정책이 성공적으로 등록되었습니다.");

        return "redirect:refund/admin/policy-form";
    }
}
