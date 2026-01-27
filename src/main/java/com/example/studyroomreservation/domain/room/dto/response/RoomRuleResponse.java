package com.example.studyroomreservation.domain.room.dto.response;

import java.time.LocalDateTime;

public record RoomRuleResponse(
        Long id,
        String name,
        Integer minDurationMinutes,
        Integer bookingOpenDays,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime activeUpdatedAt
) {}
