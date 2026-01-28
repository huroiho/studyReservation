package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.SlotUnit;

import java.time.LocalDateTime;

public record OperationPolicyListResponse(
        Long id,
        String name,
        SlotUnit slotUnit,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime activeUpdateAt
) {}
