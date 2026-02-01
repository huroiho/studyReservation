package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.response.RoomResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomImage;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    RoomResponse toResponse(Room room);

    default UserRoomListResponse toUserListResponse(Room room) {
        return new UserRoomListResponse(
                room.getId(),
                room.getName(),
                room.getMaxCapacity(),
                room.getPrice(),
                selectMainImageUrl(room.getImages())
        );
    }

    default UserRoomDetailResponse toUserDetailResponse(Room room) {
        List<String> amenities = room.getAmenities() == null
                ? List.of()
                : room.getAmenities().stream()
                        .map(Enum::name)
                        .sorted()
                        .toList();

        String heroImageUrl = selectHeroImageUrl(room.getImages());
        List<UserRoomDetailResponse.GalleryImage> galleryImages = selectGalleryImages(room.getImages());

        Integer slotMinutes = room.getOperationPolicy() != null
                ? room.getOperationPolicy().getSlotUnit().getMinutes()
                : 60;
        Integer minDurationMinutes = room.getRoomRule() != null
                ? room.getRoomRule().getMinDurationMinutes()
                : 60;
        Integer bookingOpenDays = room.getRoomRule() != null
                ? room.getRoomRule().getBookingOpenDays()
                : 30;

        return new UserRoomDetailResponse(
                room.getId(),
                room.getName(),
                room.getMaxCapacity(),
                room.getPrice(),
                slotMinutes,
                minDurationMinutes,
                bookingOpenDays,
                amenities,
                heroImageUrl,
                galleryImages
        );
    }

    @Named("selectMainImageUrl")
    default String selectMainImageUrl(List<RoomImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        // Priority: MAIN -> THUMBNAIL -> GENERAL (by lowest sortOrder)
        return findByType(images, RoomImage.ImageType.MAIN)
                .or(() -> findByType(images, RoomImage.ImageType.THUMBNAIL))
                .or(() -> findByType(images, RoomImage.ImageType.GENERAL))
                .map(RoomImage::getImageUrl)
                .orElse(null);
    }

    @Named("selectHeroImageUrl")
    default String selectHeroImageUrl(List<RoomImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        // Priority: MAIN -> GENERAL (by lowest sortOrder)
        return findByType(images, RoomImage.ImageType.MAIN)
                .or(() -> findByType(images, RoomImage.ImageType.GENERAL))
                .map(RoomImage::getImageUrl)
                .orElse(null);
    }

    default List<UserRoomDetailResponse.GalleryImage> selectGalleryImages(List<RoomImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .filter(img -> img.getType() == RoomImage.ImageType.GENERAL)
                .sorted(Comparator.comparing(RoomImage::getSortOrder))
                .map(img -> new UserRoomDetailResponse.GalleryImage(img.getId(), img.getImageUrl(), img.getSortOrder()))
                .toList();
    }

    private Optional<RoomImage> findByType(List<RoomImage> images, RoomImage.ImageType type) {
        return images.stream()
                .filter(img -> img.getType() == type)
                .min(Comparator.comparing(RoomImage::getSortOrder));
    }
}
