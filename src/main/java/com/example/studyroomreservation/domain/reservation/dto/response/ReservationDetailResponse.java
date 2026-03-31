package com.example.studyroomreservation.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record ReservationDetailResponse(
        ReservationInfo reservationInfo,
        RoomInfo roomInfo,
        MemberInfo memberInfo,
        PaymentInfo paymentInfo,
        boolean isReservationCancellable
) {

        public record ReservationInfo(
                Long id,
                String status,
                @JsonFormat(pattern = "yyyy-MM-dd")
                LocalDateTime confirmedAt,
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
                LocalDateTime startTime,
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
                LocalDateTime endTime,
                int totalAmount
        ) {}

        public record RoomInfo(
                Long id,
                String name,
                String thumbnailUrl
        ) {}

        public record MemberInfo(
                String name,
                String email
        ) {}

        public record PaymentInfo(
                String successOrderId,
                @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
                LocalDateTime approvedAt,
                int amount
        ) {}
}