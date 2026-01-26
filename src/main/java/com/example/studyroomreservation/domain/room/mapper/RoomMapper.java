package com.example.studyroomreservation.domain.room.mapper;

//import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.dto.request.RoomRuleCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.RoomResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomImage;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

//componentModel 미지정시 구현체내 별도 인스턴스 접근 호출 필요(RoomMapper INSTANCE = Mappers.getMapper(RoomMapper.class);)
@Mapper(componentModel = "spring")
public interface RoomMapper {
    // Entity -> Response DTO (조회)
    //--질문 : RoomMapper/RoomRuleMapper 분리??
    RoomResponse toResponse(Room room);
    RoomRuleResponse toRuleResponse(RoomRule rule);
    List<RoomRuleResponse> toResponseList(List<RoomRule> entities);

    // Request DTO -> Entity (등록)
    // Room + RoomImage
//    @Mapping(target = "roomRule", ignore = true) //mapping 다시보기
//    Room toRoomEntity(RoomCreateRequest request); // Room 엔티티 내부의 RoomRule 연관관계는 서비스에서 주입하거나 ID로 매핑
//    List<RoomImage> toImageEntities(List<RoomCreateRequest.RoomImageRequest> requests); // List<RoomImageRequest> -> List<RoomImage> 변환
//    RoomImage toImageEntity(RoomCreateRequest.RoomImageRequest request);

    // RoomRule
    @Mapping(target = "active", ignore = true) // 명세서 Def: T 반영
    RoomRule toRuleEntity(RoomRuleCreateRequest request);
}
