package com.example.studyroomreservation.domain.room.dto.response;

import java.util.List;

public record UserRoomListResponse(
        Long id,
        String name,
        int maxCapacity,
        int price,
        String mainImageUrl
        //List<String> amenities
) {
}
