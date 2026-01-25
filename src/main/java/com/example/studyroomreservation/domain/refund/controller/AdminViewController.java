package com.example.studyroomreservation.domain.refund.controller;

import com.example.studyroomreservation.domain.refund.service.AdminRefundPolicyService;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/refund/admin")
public class AdminViewController {

    private final RefundPolicyService refundPolicyService;

    @GetMapping("/list")
    public String refundPolicyList(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable,
            Model model
    ) {
        model.addAttribute("page", refundPolicyService.getRefundPolicyPage(pageable));
        return "refund/admin/list";
    }

    @GetMapping("/{policyId}")
    public String refundPolicyDetail(@PathVariable Long policyId, Model model) {
        model.addAttribute("policy", refundPolicyService.getRefundPolicyDetail(policyId));
        return "refund/admin/detail";
    }
}
