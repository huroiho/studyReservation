package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.SlotUnit;

import java.time.LocalTime;
import java.util.List;

public record OperationPolicyResponse(
        Long id,
        String name,
        SlotUnit slotUnit,
        List<OperationScheduleResponse> schedules
) {
}
