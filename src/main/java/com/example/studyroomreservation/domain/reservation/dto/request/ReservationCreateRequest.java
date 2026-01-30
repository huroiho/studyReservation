package com.example.studyroomreservation.domain.reservation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationCreateRequest(
        @NotNull
        Long roomId,
        @NotNull
        @Future // 미래 시간만 예약 가능하게 유효성 검사 어노테이션 추가
        LocalDateTime startTime,
        @NotNull
        @Future
        LocalDateTime endTime
) {
        @AssertTrue(message = "종료 시간은 시작 시간보다 뒤여야 합니다.")
        public boolean isEndTimeAfterStartTime() {
                if (startTime == null || endTime == null) return true;
                return endTime.isAfter(startTime);
        }
}
