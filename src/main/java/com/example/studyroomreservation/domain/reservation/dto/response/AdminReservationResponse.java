package com.example.studyroomreservation.domain.reservation.dto.response;

import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import java.time.LocalDateTime;

public record AdminReservationResponse(
        Long id,
        String memberEmail,
        String roomName,
        ReservationStatus status,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int totalAmount,
        LocalDateTime createdAt
) {
}