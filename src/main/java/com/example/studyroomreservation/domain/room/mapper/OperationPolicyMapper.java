package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
}
