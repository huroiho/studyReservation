package com.example.studyroomreservation.domain.reservation.mapper;

import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper
        (componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {


    default Reservation toTempReservation(ReservationCreateRequest request,
                                          Long memberId,
                                          Room room,
                                          Integer totalAmount,
                                          LocalDateTime expiresAt) {
        return Reservation.createTemp(
                memberId,
                room.getId(),
                room.getOperationPolicy().getId(),
                room.getRefundPolicyId(),
                request.startTime(),
                request.endTime(),
                totalAmount,
                expiresAt
        );
    }
}