package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.RoomSummary;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyListResponse;
import com.example.studyroomreservation.domain.room.service.OperationPolicyService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.studyroomreservation.domain.room.controller.OperationPolicyControllerConstants.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_BASE_PATH)
public class OperationPolicyRestController {

    private final OperationPolicyService operationPolicyService;

    // 활성화된 전체 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<OperationPolicyListResponse>>> listActive(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Page<OperationPolicyListResponse> result = operationPolicyService.getList(keyword, true, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 운영정책 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OperationPolicyDetailResponse>> getDetail(@PathVariable Long id) {
        OperationPolicyDetailResponse result = operationPolicyService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 특정 정책을 사용하는 룸 목록 조회(모달용)
    @GetMapping(ROOMS_BY_POLICY)
    public ResponseEntity<List<RoomSummary>> getRoomsByPolicy(@PathVariable Long policyId) {
        List<RoomSummary> summaries = operationPolicyService.getRoomsByPolicy(policyId);
        return ResponseEntity.ok(summaries);
    }
}
