package com.example.studyroomreservation.domain.room.dto.request;

import com.example.studyroomreservation.domain.room.entity.SlotUnit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record OperationPolicyCreateRequest(
        @NotBlank(message = "정책명을 입력해주세요.")
        @Size(min = 2, max = 50, message = "정책명은 2~50자 사이로 입력해주세요.")
        String name,

        @NotNull(message = "슬롯 단위를 선택해주세요.")
        SlotUnit slotUnit,

        @Valid
        @NotNull(message = "요일별 스케줄이 필요합니다.")
        @Size(min = 7, max = 7, message = "요일별 스케줄은 월~일 7개가 필요합니다.")
        List<ScheduleRequest> schedules
) {
    public record ScheduleRequest(

            @NotNull(message = "요일 정보가 필요합니다.")
            DayOfWeek dayOfWeek,

            LocalTime openTime,
            LocalTime closeTime,

            boolean closed
    ) {
        @AssertTrue(message = "운영일에는 오픈/마감 시간을 모두 선택해야 합니다.")
        public boolean isTimesPresentWhenOpen() {
            if (closed) return true;
            return openTime != null && closeTime != null;
        }

        @AssertTrue(message = "오픈/마감 시간은 정각(:00)만 선택 가능합니다.")
        public boolean isHourOnlyWhenOpen() {
            if (closed) return true;
            if (openTime == null || closeTime == null) return true; // 위 검증에서 잡힘
            return isHourOnly(openTime) && isHourOnly(closeTime);
        }

        @AssertTrue(message = "오픈 시간은 마감 시간보다 빨라야 합니다.")
        public boolean isOpenBeforeCloseWhenOpen() {
            if (closed) return true;
            if (openTime == null || closeTime == null) return true;
            return openTime.isBefore(closeTime);
        }

        private boolean isHourOnly(LocalTime t) {
            return t.getMinute() == 0 && t.getSecond() == 0 && t.getNano() == 0;
        }
    }
}
