package com.example.studyroomreservation.domain.room.dto.request;

import com.example.studyroomreservation.domain.room.entity.Room.AmenityType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoomCreateRequest(
        @NotBlank(message = "방 이름은 필수입니다.")
        @Size(min = 2, max = 100, message = "방 이름은 2~100자 사이로 입력해주세요.")
        String name,

        @NotNull(message = "최대 수용 인원은 필수입니다.")
        @Min(value = 1, message = "최대 수용 인원은 1명 이상이어야 합니다.")
        Integer maxCapacity,

        @NotNull(message = "시간당 가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "운영 정책을 선택해주세요.")
        Long operationPolicyId,

        @NotNull(message = "예약 규칙을 선택해주세요.")
        Long roomRuleId,

        @NotNull(message = "환불 정책을 선택해주세요.")
        Long refundPolicyId,

        Set<AmenityType> amenities
) {
    public RoomCreateRequest {
        if (amenities == null) {
            amenities = Set.of();
        }
    }
}
