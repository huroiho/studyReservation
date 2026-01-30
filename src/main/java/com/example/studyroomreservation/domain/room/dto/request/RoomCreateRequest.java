package com.example.studyroomreservation.domain.room.dto.request;

import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomImage;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

public record RoomCreateRequest(
        @NotNull(message = "운영정책을 선택해주세요.")
        Long operationPolicyId,

        @NotNull(message = "룸 규칙을 선택해주세요.")
        Long roomRuleId,

        @NotNull(message = "환불정책을 선택해주세요.")
        Long refundPolicyId,

        @NotBlank(message = "룸 이름은 필수입니다.")
        @Size(max = 20, message = "룸 이름은 100자 이하로 입력해주세요.")
        String name,

        @NotNull(message = "최대 수용 인원은 필수입니다.")
        @Min(value = 1, message = "최대 수용 인원은 1명 이상이어야 합니다.")
        Integer maxCapacity,

        @NotNull(message = "가격은 필수입니다.")
        @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
        Integer price,

        Set<Room.AmenityType> amenities,

        @NotNull(message = "이미지는 필수입니다.")
        @Min(value = 1, message = "이미지는 1장 이상 등록해야 합니다.")
        @Valid
        List<RoomImageDraft> images
) {
    public record RoomImageDraft(

            @NotBlank(message = "이미지 URL은 필수입니다.")
            String imageUrl,

            @NotNull(message = "이미지 타입은 필수입니다.")
            RoomImage.ImageType type,

            @NotNull(message = "이미지 정렬 순서는 필수입니다.")
            @Min(value = 0, message = "이미지 정렬 순서는 0 이상이어야 합니다.")
            Integer sortOrder
    ) {}
}