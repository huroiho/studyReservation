package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.*;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.DeleteBlockInfo;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.RoomSummary;
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
    OperationScheduleResponse toScheduleDetail(OperationSchedule schedule);

    default List<OperationScheduleResponse> toScheduleDetails(List<OperationSchedule> schedules) {
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

        List<OperationScheduleResponse> schedules = toScheduleDetails(policy.getSchedules());
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

    // pick-detail 응답 조립
    default OperationPolicyPickDetailResponse toPickDetailResponse(OperationPolicy policy) {
        if (policy == null) return null;

        List<OperationScheduleResponse> schedules =
                policy.getSchedules() == null ? List.of()
                        : policy.getSchedules().stream()
                        .sorted(Comparator.comparingInt(s -> s.getDayOfWeek().getValue()))
                        .map(this::toScheduleDetail)
                        .toList();

        return new OperationPolicyPickDetailResponse(
                policy.getId(),
                policy.getName(),
                policy.getSlotUnit(),
                policy.isActive(),
                policy.getCreatedAt(),
                schedules
        );
    }

    default OperationPolicyResponse toResponse(OperationPolicy policy) {
        if (policy == null) return null;

        List<OperationScheduleResponse> schedules = toScheduleDetails(policy.getSchedules());

        return new OperationPolicyResponse(
                policy.getId(),
                policy.getName(),
                policy.getSlotUnit(),
                schedules
        );
    }
}
