package com.example.studyroomreservation.domain.room.dto.response;

import java.util.List;

public record UserRoomDetailResponse(
        Long id,
        String name,
        Integer maxCapacity,
        Integer price,
        Integer slotMinutes,
        Integer minDurationMinutes,
        Integer bookingOpenDays,
        List<String> amenities,
        String heroImageUrl,
        List<GalleryImage> galleryImages
) {
    public record GalleryImage(
            Long id,
            String url,
            Integer sortOrder
    ) {}
}
