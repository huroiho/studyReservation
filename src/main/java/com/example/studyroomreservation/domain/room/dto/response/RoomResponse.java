package com.example.studyroomreservation.domain.room.dto.response;

import java.util.List;

public record RoomResponse(
        Long id,
        String name,
        Integer maxCapacity,
        Integer price,
        List<String> amenities,
        String status, // ACTIVE 등
        RoomRuleResponse rule, // 연결된 규칙 정보
        List<RoomImageResponse> images
) {
    public record RoomImageResponse(
            Long id,
            String imageUrl,
            String type,
            int sortOrder
    ) {}
}
