package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.RoomSummary;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyPickDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyPickItemResponse;
import com.example.studyroomreservation.domain.room.service.OperationPolicyService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.studyroomreservation.domain.room.controller.OperationPolicyControllerConstants.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(API_BASE_PATH)
public class OperationPolicyRestController {

    private final OperationPolicyService operationPolicyService;

    // Room 생성 시 활성화 된 목록 조회용
    @GetMapping("/pick-items")
    public ResponseEntity<ApiResponse<List<OperationPolicyPickItemResponse>>> pickItems() {
        return ResponseEntity.ok(ApiResponse.success(operationPolicyService.getActivePickItems()));
    }

    // 룸 생성 시 운영정책 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OperationPolicyPickDetailResponse>> getDetail(@PathVariable Long id) {
        OperationPolicyPickDetailResponse result = operationPolicyService.getPickDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // 특정 정책을 사용하는 룸 목록 조회(모달용)
    @GetMapping(ROOMS_BY_POLICY)
    public ResponseEntity<List<RoomSummary>> getRoomsByPolicy(@PathVariable Long policyId) {
        List<RoomSummary> summaries = operationPolicyService.getRoomsByPolicy(policyId);
        return ResponseEntity.ok(summaries);
    }
}
