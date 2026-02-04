package com.example.studyroomreservation.domain.room.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OperationScheduleResponse(
        DayOfWeek dayOfWeek,
        boolean closed,
        LocalTime openTime,
        LocalTime closeTime
) {}