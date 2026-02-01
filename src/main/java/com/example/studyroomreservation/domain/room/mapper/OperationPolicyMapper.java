package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.DeleteBlockInfo;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.ScheduleDetail;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.RoomSummary;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.OperationSchedule;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OperationPolicyMapper {

    @Mapping(target = "isClosed", source = "closed")
    OperationPolicy.ScheduleDraft toDraft(OperationPolicyCreateRequest.ScheduleRequest s);

    List<OperationPolicy.ScheduleDraft> toDrafts(
            List<OperationPolicyCreateRequest.ScheduleRequest> schedules
    );

    default OperationPolicy createPolicy(OperationPolicyCreateRequest request){
        if(request == null) return null;

        List<OperationPolicy.ScheduleDraft> drafts = toDrafts(request.schedules());

        return OperationPolicy.createWith7Days(
                request.name(),
                request.slotUnit(),
                drafts
        );
    }

    @Mapping(target = "closed", source = "closed")
    ScheduleDetail toScheduleDetail(OperationSchedule schedule);

    default List<ScheduleDetail> toScheduleDetails(List<OperationSchedule> schedules) {
        if (schedules == null) return List.of();

        // 요일순 정렬 (MONDAY=1 ... SUNDAY=7)
        return schedules.stream()
                .sorted(Comparator.comparingInt(s -> s.getDayOfWeek().getValue()))
                .map(this::toScheduleDetail)
                .toList();
    }

    @Mapping(target = "status", expression = "java(room.getStatus().name())")
    RoomSummary toRoomSummary(Room room);

    default List<RoomSummary> toRoomSummaries(List<Room> rooms) {
        if (rooms == null) return List.of();
        return rooms.stream()
                .map(this::toRoomSummary)
                .toList();
    }

    default OperationPolicyDetailResponse toDetailResponse(
            OperationPolicy policy,
            List<Room> connectedRooms,
            boolean hasReservationReference
    ) {
        if (policy == null) return null;

        List<ScheduleDetail> schedules = toScheduleDetails(policy.getSchedules());
        List<RoomSummary> rooms = toRoomSummaries(connectedRooms);

        int connectedRoomCount = rooms.size();
        boolean deletable = connectedRoomCount == 0 && !hasReservationReference;
        DeleteBlockInfo deleteInfo = new DeleteBlockInfo(connectedRoomCount, hasReservationReference, deletable);

        return new OperationPolicyDetailResponse(
                policy.getId(),
                policy.getName(),
                policy.getSlotUnit(),
                policy.isActive(),
                policy.getCreatedAt(),
                policy.getActiveUpdatedAt(),
                schedules,
                rooms,
                deleteInfo
        );
    }

    default OperationPolicyResponse toOperationPolicyResponseForRoom(OperationPolicy policy) {
        if ( policy == null ) {
            return null;
        }

        List<OperationPolicyResponse.ScheduleResponse> schedules = policy.getSchedules().stream()
                .map(s -> new OperationPolicyResponse.ScheduleResponse(
                        s.getDayOfWeek(),
                        s.getOpenTime(),
                        s.getCloseTime(),
                        s.isClosed()
                )).toList();

        return new OperationPolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getSlotUnit(),
                schedules
        );
    }
}
