package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse.ScheduleDetailResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.OperationSchedule;
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

    @Mapping(target = "schedules", expression = "java(toScheduleDetailResponses(policy.getSchedules()))")
    OperationPolicyDetailResponse toDetailResponse(OperationPolicy policy);

    @Mapping(target = "closed", source = "closed")
    ScheduleDetailResponse toScheduleDetailResponse(OperationSchedule schedule);

    default List<ScheduleDetailResponse> toScheduleDetailResponses(List<OperationSchedule> schedules) {
        if (schedules == null) return List.of();

        // Sort by DayOfWeek (MONDAY=1 ... SUNDAY=7)
        return schedules.stream()
                .sorted(Comparator.comparingInt(s -> s.getDayOfWeek().getValue()))
                .map(this::toScheduleDetailResponse)
                .toList();
    }
}
