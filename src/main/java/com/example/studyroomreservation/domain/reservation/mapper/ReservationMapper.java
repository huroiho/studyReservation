package com.example.studyroomreservation.domain.reservation.mapper;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
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

    // 예약 상세 정보 dto 매핑
    ReservationDetailResponse toDetailResponse(Reservation reservationInfo,
                                               Room roomInfo,
                                               Member memberInfo,
                                               Payment paymentInfo,
                                               boolean isReservationCancellable);

    ReservationDetailResponse.ReservationInfo toReservationInfo(Reservation reservation);
    ReservationDetailResponse.RoomInfo toRoomInfo(Room room);
    ReservationDetailResponse.MemberInfo toMemberInfo(Member member);
    ReservationDetailResponse.PaymentInfo toPaymentInfo(Payment payment);
}