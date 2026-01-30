package com.example.studyroomreservation.domain.refund.controller;


import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.domain.refund.validation.RefundPolicyValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;

@Slf4j
@Controller
@RequestMapping("/admin/refund")
@RequiredArgsConstructor
public class AdminRefundViewController {

    private final RefundPolicyService refundPolicyService;
    private final RefundPolicyValidator refundPolicyValidator;

    @InitBinder("policyForm")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(refundPolicyValidator);
    }

    @GetMapping("/policy/new")
    public String createForm(Model model) {

        // 폼 초기화해서 오류 방지 rules 리스트를 비어있는 상태로라도 넘겨줘야 함
        model.addAttribute("policyForm", new RefundPolicyRequest(null, new ArrayList<>()));
        return "refund/admin/policy-form";
    }

    @PostMapping("/policy/new")
    public String register(
            @Valid @ModelAttribute("policyForm") RefundPolicyRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            log.debug("Validation errors: {}", bindingResult.getAllErrors());
            return "refund/admin/policy-form";
        }

        Long savedId = refundPolicyService.registerPolicy(request);
        log.info("Refund policy created: id={}, name={}", savedId, request.name());
        redirectAttributes.addFlashAttribute("message", "환불 정책이 성공적으로 등록되었습니다.");

        return "redirect:/admin/refund/policy/" + savedId;
    }

    @GetMapping("/policy/list")
    public String refundPolicyList(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model
    ) {
        model.addAttribute("active", active);
        model.addAttribute("page", refundPolicyService.getRefundPolicyPage(active,pageable));
        return "refund/admin/list";
    }

    @GetMapping("/policy/{policyId}")
    public String refundPolicyDetail(@PathVariable Long policyId, Model model) {
        model.addAttribute("policy", refundPolicyService.getRefundPolicyDetail(policyId));
        return "refund/admin/detail";
    }

    @PostMapping("/policy/{policyId}/active")
    public String changePolicyActive(@PathVariable Long policyId, @RequestParam boolean active) {
        refundPolicyService.changePolicyActive(policyId, active);
        return "redirect:/admin/refund/policy/list";
    }
}
