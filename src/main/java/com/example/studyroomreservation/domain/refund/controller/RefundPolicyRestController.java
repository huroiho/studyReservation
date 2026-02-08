package com.example.studyroomreservation.domain.refund.controller;

import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyDetailResponse;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.example.studyroomreservation.domain.refund.controller.RefundConstants.API_REFUND_POLICY_BASE;
import static com.example.studyroomreservation.domain.refund.controller.RefundConstants.API_REFUND_POLICY_DETAIL;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_REFUND_POLICY_BASE)
public class RefundPolicyRestController {

    private final RefundPolicyService refundPolicyService;

    @GetMapping(API_REFUND_POLICY_DETAIL)
    public ApiResponse<RefundPolicyDetailResponse> getRefundPolicyDetail(@PathVariable Long policyId) {
        RefundPolicyDetailResponse response = refundPolicyService.getRefundPolicyDetail(policyId);
        return ApiResponse.success(response);
    }
}
