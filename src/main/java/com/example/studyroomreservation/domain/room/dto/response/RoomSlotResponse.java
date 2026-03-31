package com.example.studyroomreservation.domain.room.dto.response;

import java.time.LocalTime;

public record RoomSlotResponse(
        LocalTime startTime,
        LocalTime endTime,
        SlotStatus status
) {
    public enum SlotStatus {
        AVAILABLE,
        RESERVED,
        UNAVAILABLE  // 오늘 날짜에서 현재 시간 이전의 슬롯
    }
}
