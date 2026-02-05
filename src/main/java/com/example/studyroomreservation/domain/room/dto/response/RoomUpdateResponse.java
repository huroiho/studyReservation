package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.Room.AmenityType;
import com.example.studyroomreservation.domain.room.entity.RoomImage.ImageType;

import java.util.List;
import java.util.Set;

public record RoomUpdateResponse(
    Long id,
    String name,
    Integer maxCapacity,
    Integer price,
    Set<AmenityType> amenities,

    Long operationPolicyId,
    String operationPolicyName,
    Long roomRuleId,
    String roomRuleName,
    Long refundPolicyId,
    String refundPolicyName,

    List<ImageItem> images
) {
    public record ImageItem(
            Long id, String imageUrl, ImageType type, int sortOrder
    ) {}
}