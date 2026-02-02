package com.example.studyroomreservation.domain.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

// TODO: RoomReservedTimeResponse 명칭 재검토 필요
// 실제로는 예약된(차단된) 시간 구간을 의미함
public record RoomReservedTimeResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime startTime,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime endTime
) {
}
