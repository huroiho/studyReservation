package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.SlotUnit;

import java.time.LocalDateTime;
import java.util.List;

public record OperationPolicyPickDetailResponse(
        Long id,
        String name,
        SlotUnit slotUnit,
        boolean active,
        LocalDateTime createdAt,
        List<OperationScheduleResponse> schedules
) {}