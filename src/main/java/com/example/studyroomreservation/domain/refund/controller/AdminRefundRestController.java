package com.example.studyroomreservation.domain.refund.controller;

import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyDetailResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyPickItemResponse;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.studyroomreservation.domain.refund.controller.RefundConstants.*;

@RestController
@RequestMapping(API_ADMIN_REFUND_BASE)
@RequiredArgsConstructor
public class AdminRefundRestController {

    private final RefundPolicyService refundPolicyService;

    // Room 생성 시 활성화 된 목록 조회용
    @GetMapping(API_REFUND_PICK)
    public ResponseEntity<ApiResponse<List<RefundPolicyPickItemResponse>>> pickItems() {
        return ResponseEntity.ok(ApiResponse.success(refundPolicyService.getActivePickItems()));
    }

    // 상세조회
    @GetMapping(API_REFUND_DETAIL)
    public ResponseEntity<ApiResponse<RefundPolicyDetailResponse>> getDetail(@PathVariable Long id) {
        RefundPolicyDetailResponse result = refundPolicyService.getRefundPolicyDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
