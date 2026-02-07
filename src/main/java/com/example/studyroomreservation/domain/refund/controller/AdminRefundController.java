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

import static com.example.studyroomreservation.domain.refund.controller.RefundConstants.*;

@Slf4j
@Controller
@RequestMapping(VIEW_ADMIN_REFUND_BASE)
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundPolicyService refundPolicyService;
    private final RefundPolicyValidator refundPolicyValidator;


    @InitBinder("policyForm")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(refundPolicyValidator);
    }

    @GetMapping(VIEW_ADMIN_REFUND_POLICY_NEW)
    public String createForm(Model model) {

        // 폼 초기화해서 오류 방지 rules 리스트를 비어있는 상태로라도 넘겨줘야 함
        model.addAttribute("policyForm", new RefundPolicyRequest(null, new ArrayList<>()));
        return TMPL_ADMIN_REFUND_FORM;
    }

    @PostMapping(VIEW_ADMIN_REFUND_POLICY_NEW)
    public String register(
            @Valid @ModelAttribute("policyForm") RefundPolicyRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            log.debug("Validation errors: {}", bindingResult.getAllErrors());
            return TMPL_ADMIN_REFUND_FORM;
        }

        Long savedId = refundPolicyService.registerPolicy(request);
        log.info("Refund policy created: id={}, name={}", savedId, request.name());
        redirectAttributes.addFlashAttribute("message", "환불 정책이 성공적으로 등록되었습니다.");

        return redirectRefundPolicyDetail(savedId);
    }

    @GetMapping(VIEW_ADMIN_REFUND_POLICY_LIST)
    public String refundPolicyList(
            @RequestParam(required = false) Boolean active,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model
    ) {
        model.addAttribute("active", active);
        model.addAttribute("page", refundPolicyService.getRefundPolicyPage(active,pageable));
        return TMPL_ADMIN_REFUND_LIST;
    }

    @GetMapping(VIEW_ADMIN_REFUND_POLICY_DETAIL)
    public String refundPolicyDetail(@PathVariable Long policyId, Model model) {
        model.addAttribute("policy", refundPolicyService.getRefundPolicyDetail(policyId));
        return TMPL_ADMIN_REFUND_DETAIL;
    }

    @PostMapping(VIEW_ADMIN_REFUND_POLICY_ACTIVE)
    public String changePolicyActive(@PathVariable Long policyId, @RequestParam boolean active) {
        refundPolicyService.changePolicyActive(policyId, active);
        return REDIRECT_ADMIN_REFUND_LIST;
    }
}
