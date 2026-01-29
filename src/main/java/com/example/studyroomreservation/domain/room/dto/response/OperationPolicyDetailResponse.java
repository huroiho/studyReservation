package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.SlotUnit;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OperationPolicyDetailResponse(
        Long id,
        String name,
        SlotUnit slotUnit,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime activeUpdatedAt,
        List<ScheduleDetailResponse> schedules
) {
    public record ScheduleDetailResponse(
            DayOfWeek dayOfWeek,
            boolean closed,
            LocalTime openTime,
            LocalTime closeTime
    ) {}
}
