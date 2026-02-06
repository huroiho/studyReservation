package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomSlotResponse;
import com.example.studyroomreservation.domain.room.service.RoomService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static com.example.studyroomreservation.domain.room.controller.RoomConstants.*;

@RestController
@RequestMapping(API_ROOM_BASE)
@RequiredArgsConstructor
public class RoomApiController {

    private final RoomService roomService;

    @GetMapping(API_ROOM_SLOTS)
    public ApiResponse<List<RoomSlotResponse>> getRoomSlots(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<RoomSlotResponse> slots = roomService.getRoomSlots(roomId, date);
        return ApiResponse.success(slots);
    }

    @GetMapping(API_ROOM_POLICY)
    public ApiResponse<OperationPolicyResponse> getRoomPolicy(@PathVariable Long roomId) {
        OperationPolicyResponse policy = roomService.getRoomPolicy(roomId);
        return ApiResponse.success(policy);
    }
}
