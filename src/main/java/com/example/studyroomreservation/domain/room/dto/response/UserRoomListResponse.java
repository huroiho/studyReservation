package com.example.studyroomreservation.domain.room.dto.response;

public record UserRoomListResponse(
        Long id,
        String name,
        int maxCapacity,
        int price,
        String thumbnailImageUrl
) {
}
