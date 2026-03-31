package com.example.studyroomreservation.domain.reservation.dto.response;

import java.time.Duration;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        Long roomId,
        String roomName,
        String roomImageUrl,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        int totalAmount,
        long durationHours
) {
    // 30분단위 표현
    public String getDurationText() {
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (minutes > 0) {
            return hours + "시간 " + minutes + "분";
        }
        return hours + "시간";
    }
}