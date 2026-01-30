package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.RoomSummary;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.domain.room.service.OperationPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.example.studyroomreservation.domain.room.controller.OperationPolicyControllerConstants.*;

/**
 * 운영 정책 관련 API 엔드포인트
 * - 목록 페이지에서 모달용 데이터 조회 등에 사용
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(API_BASE_PATH)
public class OperationPolicyApiController {

    private final OperationPolicyService operationPolicyService;

    // 특정 정책을 사용하는 룸 목록 조회(모달용)
    @GetMapping(ROOMS_BY_POLICY)
    public ResponseEntity<List<RoomSummary>> getRoomsByPolicy(@PathVariable Long policyId) {
        List<RoomSummary> summaries = operationPolicyService.getRoomsByPolicy(policyId);
        return ResponseEntity.ok(summaries);
    }
}
