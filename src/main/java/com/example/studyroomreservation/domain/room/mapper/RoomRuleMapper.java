package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.request.RoomRuleCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomRuleMapper {
    // Entity -> Response DTO (조회)
    @Mapping(source = "active", target = "isActive")
    RoomRuleResponse toRuleResponse(RoomRule rule);

    // Request DTO -> Entity (등록)
    default RoomRule createRoomRule(RoomRuleCreateRequest request) {
        if(request == null){
            return null;
        }
        RoomRule roomRule = RoomRule.createRoomRule(
                request.name(),
                request.minDurationMinutes(),
                request.bookingOpenDays(),
                request.active()
        );

        return roomRule;
    }
}
