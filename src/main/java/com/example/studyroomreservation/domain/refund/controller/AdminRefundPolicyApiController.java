package com.example.studyroomreservation.domain.refund.controller;

import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyDetailResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyListResponse;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/refund-policies")
@RequiredArgsConstructor
public class AdminRefundPolicyApiController {

    private final RefundPolicyService refundPolicyService;

    // Room 생성 시 활성화 된 목록 조회용
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RefundPolicyListResponse>>> listActive(Pageable pageable) {
        // isActive = true로 고정하여 활성화된 정책만 조회
        Page<RefundPolicyListResponse> result = refundPolicyService.getRefundPolicyPage(true, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 상세조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RefundPolicyDetailResponse>> getDetail(@PathVariable Long id) {
        RefundPolicyDetailResponse result = refundPolicyService.getRefundPolicyDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
