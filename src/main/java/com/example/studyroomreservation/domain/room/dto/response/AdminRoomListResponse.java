package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.Room.RoomStatus;

public record  AdminRoomListResponse(
        Long id,
        String name,
        Integer maxCapacity,
        Integer price,
        RoomStatus status,
        String thumbnailUrl
) {}
