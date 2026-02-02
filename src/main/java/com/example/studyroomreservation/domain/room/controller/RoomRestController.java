package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyResponse;
import com.example.studyroomreservation.domain.room.service.RoomService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomRestController {

    private final RoomService roomService;

    @GetMapping("/{roomId}/policy")
    public ApiResponse<OperationPolicyResponse> getRoomPolicy(@PathVariable Long roomId) {
         OperationPolicyResponse policy = roomService.getRoomPolicy(roomId);
        return ApiResponse.success(policy);
    }
}
