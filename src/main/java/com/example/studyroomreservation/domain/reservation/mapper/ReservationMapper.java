package com.example.studyroomreservation.domain.reservation.mapper;

import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;

@Mapper
public interface ReservationMapper {

    // Entity -> Response DTO (조회)
    @Mapping(source = "reservation.id", target = "reservationId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(target = "roomImageUrl", expression = "java(getThumbnailUrl(room))") //룸 객체 전체를 넘겨 getThumbnailUrl 메서드가 처리
    @Mapping(source = "reservation.status", target = "status")
    @Mapping(target = "durationHours", expression = "java(java.time.Duration.between(reservation.getStartTime(), reservation.getEndTime()).toHours())")
    ReservationResponse toResponse(Reservation reservation, Room room);

    // 대표 룸 이미지 하나
    default String getThumbnailUrl(Room room) {
        // 이미지가 없는 경우 (필수니까 생략??)
        if (room == null || room.getImages() == null || room.getImages().isEmpty()) {
            return null;
        }

        // MAIN 또는 THUMBNAIL 타입 이미지 찾기
        // 없으면 리스트의 첫 번째 이미지
        return room.getImages().stream()
                .filter(img -> img.getType() == RoomImage.ImageType.MAIN || img.getType() == RoomImage.ImageType.THUMBNAIL)
                .map(RoomImage::getImageUrl)
                .findFirst()
                .orElse(room.getImages().get(0).getImageUrl());
    }

    List<ReservationResponse> toResponseList(List<Reservation> reservations);


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
                                               Member memberInfo,
                                               Payment paymentInfo,
                                               boolean isReservationCancellable,
                                               Room roomInfo);

    ReservationDetailResponse.ReservationInfo toReservationInfo(Reservation reservation);

    @Mapping(target = "thumbnailUrl", expression = "java(room.getThumbnailUrl())")
    ReservationDetailResponse.RoomInfo toRoomInfo(Room room);
    ReservationDetailResponse.MemberInfo toMemberInfo(Member member);
    ReservationDetailResponse.PaymentInfo toPaymentInfo(Payment payment);
}