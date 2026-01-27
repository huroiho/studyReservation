package com.example.studyroomreservation.domain.room.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRuleCreateRequest (
        @NotBlank(message = "규칙 명칭은 필수입니다.")
        String name,

        @NotNull(message = "최소 이용 시간은 필수입니다.")
        @Min(value = 0, message = "최소 이용 시간은 0분 이상이어야 합니다.")
        Integer minDurationMinutes,

        @NotNull(message = "예약 가능 기간은 필수입니다.")
        @Min(value = 0, message = "예약 가능 기간은 0일 이상이어야 합니다.")
        Integer bookingOpenDays
){}
