package com.example.studyroomreservation.domain.room.dto.response;
import com.example.studyroomreservation.domain.room.entity.Room;
import java.util.Set;

public record UserRoomListResponse(
        Long id,
        String name,
        int maxCapacity,
        int price,
        String mainImageUrl,
        Set<Room.AmenityType> amenities // 중복없는 데이터라 List보다 Set
) {
}
