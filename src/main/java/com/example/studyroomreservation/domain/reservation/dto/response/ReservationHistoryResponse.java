package com.example.studyroomreservation.domain.reservation.dto.response;

import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ReservationHistoryResponse(
        Long reservationId,
        String roomName,
        String roomImageUrl,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime startTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
        LocalDateTime endTime,
        ReservationStatus status,
        int totalAmount
) {
}
