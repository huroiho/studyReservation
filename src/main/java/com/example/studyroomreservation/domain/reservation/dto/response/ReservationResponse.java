package com.example.studyroomreservation.domain.reservation.dto.response;

import com.example.studyroomreservation.domain.reservation.entity.Reservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        Long roomId,
        String roomName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        int totalAmount
) {}
