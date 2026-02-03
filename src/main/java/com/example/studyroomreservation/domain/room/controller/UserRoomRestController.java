package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.RoomSlotResponse;
import com.example.studyroomreservation.domain.room.service.RoomService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class UserRoomRestController {

    private final RoomService roomService;

    @GetMapping("/{roomId}/slots")
    public ApiResponse<List<RoomSlotResponse>> getRoomSlots(@PathVariable Long roomId, @RequestParam LocalDate date) {
        List<RoomSlotResponse> slots = roomService.getRoomSlots(roomId, date);
        return ApiResponse.success(slots);
    }
}
