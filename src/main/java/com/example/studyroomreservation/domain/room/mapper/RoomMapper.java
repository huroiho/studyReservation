package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.response.RoomResponse;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.mapstruct.Mapper;

//componentModel 미지정시 구현체내 별도 인스턴스 접근 호출 필요(RoomMapper INSTANCE = Mappers.getMapper(RoomMapper.class);)
@Mapper(componentModel = "spring")
public interface RoomMapper {
    // Entity -> Response DTO (조회)
    RoomResponse toResponse(Room room);

    // Request DTO -> Entity (등록)

}
