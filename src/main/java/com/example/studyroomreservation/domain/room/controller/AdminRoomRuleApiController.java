package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/room-rules")
@RequiredArgsConstructor
public class AdminRoomRuleApiController {

    private final RoomRuleService roomRuleService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<RoomRuleResponse>>> listActive(
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<RoomRuleResponse> result = roomRuleService.getActiveRoomRules(page);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomRuleResponse>> getDetail(@PathVariable Long id) {
        RoomRuleResponse result = roomRuleService.getRuleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
