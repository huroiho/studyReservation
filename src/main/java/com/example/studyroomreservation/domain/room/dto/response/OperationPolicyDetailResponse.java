package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.SlotUnit;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public record OperationPolicyDetailResponse(
        Long id,
        String name,
        SlotUnit slotUnit,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime activeUpdatedAt,
        List<ScheduleDetail> schedules,
        List<RoomSummary> rooms,
        DeleteBlockInfo deleteInfo
) {
    // 요일별 운영 스케줄 정보
    public record ScheduleDetail(
            DayOfWeek dayOfWeek,
            boolean closed,
            LocalTime openTime,
            LocalTime closeTime
    ) {}

    // 연결된 룸 요약 정보
    public record RoomSummary(
            Long id,
            String name,
            String status
    ) {}

    // 삭제 메시지 조합을 위한 제약 조건 정보
    public record DeleteBlockInfo(
            int connectedRoomCount,
            boolean hasReservationReference,
            boolean deletable
    ) {}
}
