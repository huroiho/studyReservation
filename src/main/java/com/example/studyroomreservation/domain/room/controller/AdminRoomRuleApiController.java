package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.RoomRulePickItemResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/room-rules")
@RequiredArgsConstructor
public class AdminRoomRuleApiController {

    private final RoomRuleService roomRuleService;

    // Room 생성 시 활성화 된 목록 조회용
    @GetMapping("/pick-items")
    public ResponseEntity<ApiResponse<List<RoomRulePickItemResponse>>> pickItems() {
        return ResponseEntity.ok(ApiResponse.success(roomRuleService.getActivePickItems()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomRuleResponse>> getDetail(@PathVariable Long id) {
        RoomRuleResponse result = roomRuleService.getRuleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
