package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.RoomRulePickItemResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.studyroomreservation.domain.room.controller.RoomConstants.*;

@RestController
@RequestMapping(API_ADMIN_ROOM_RULE_BASE)
@RequiredArgsConstructor
public class AdminRoomRuleRestController {

    private final RoomRuleService roomRuleService;

    // 상태변경
    @PatchMapping(API_ROOM_RULE_STATUS)
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        roomRuleService.updateStatus(id, active);
        return ResponseEntity.ok().build();
    }

    // Room 생성 시 활성화 된 목록 조회용
    @GetMapping(API_ROOM_RULE_PICK)
    public ResponseEntity<ApiResponse<List<RoomRulePickItemResponse>>> pickItems() {
        return ResponseEntity.ok(ApiResponse.success(roomRuleService.getActivePickItems()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomRuleResponse>> getDetail(@PathVariable Long id) {
        RoomRuleResponse result = roomRuleService.getRuleDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
