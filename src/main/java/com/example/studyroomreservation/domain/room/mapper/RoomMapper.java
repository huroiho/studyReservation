package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.RoomUpdateResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomDetailResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomImage;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    int DEFAULT_SLOT_MINUTES = 60;
    int DEFAULT_MIN_DURATION_MINUTES = 60;
    int DEFAULT_BOOKING_OPEN_DAYS = 30;

    // User: Room -> UserRoomDetailResponse
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
                : DEFAULT_SLOT_MINUTES;
        Integer minDurationMinutes = room.getRoomRule() != null
                ? room.getRoomRule().getMinDurationMinutes()
                : DEFAULT_MIN_DURATION_MINUTES;
        Integer bookingOpenDays = room.getRoomRule() != null
                ? room.getRoomRule().getBookingOpenDays()
                : DEFAULT_BOOKING_OPEN_DAYS;

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

    // 대표 이미지 선택
    default String selectHeroImageUrl(List<RoomImage> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        return findByType(images, RoomImage.ImageType.MAIN)
                .or(() -> findByType(images, RoomImage.ImageType.GENERAL))
                .map(RoomImage::getImageUrl)
                .orElse(null);
    }

    // 갤러리 이미지 선택 (GENERAL만)
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

    // 타입별 가장 앞 sortOrder 1개
    private Optional<RoomImage> findByType(List<RoomImage> images, RoomImage.ImageType type) {
        return images.stream()
                .filter(img -> img.getType() == type)
                .min(Comparator.comparing(RoomImage::getSortOrder));
    }

    // ============ Admin: Create ================
    default Room toEntity(
            RoomCreateRequest request,
            OperationPolicy operationPolicy,
            RoomRule roomRule,
            Long refundPolicyId
    ) {
        return Room.create(
                operationPolicy,
                roomRule,
                refundPolicyId,
                request.name(),
                request.maxCapacity(),
                request.price(),
                request.amenities()
        );
    }

    // === Admin: Edit (Room -> RoomUpdateResponse) ===
    @Mapping(target = "operationPolicyId", source = "room.operationPolicy.id")
    @Mapping(target = "operationPolicyName", source = "room.operationPolicy.name")
    @Mapping(target = "roomRuleId", source = "room.roomRule.id")
    @Mapping(target = "roomRuleName", source = "room.roomRule.name")
    @Mapping(target = "refundPolicyId", source = "room.refundPolicyId")
    @Mapping(target = "refundPolicyName", source = "refundPolicyName")
    @Mapping(target = "images", source = "room.images", qualifiedByName = "mapImagesForUpdate")
    RoomUpdateResponse toRoomUpdateResponse(Room room, String refundPolicyName);

    @Named("mapImagesForUpdate")
    default List<RoomUpdateResponse.ImageItem> mapImagesForUpdate(List<RoomImage> images) {
        if (images == null || images.isEmpty()) return List.of();

        return images.stream()
                .sorted(Comparator.comparing(RoomImage::getSortOrder))
                .map(img -> new RoomUpdateResponse.ImageItem(
                        img.getId(),
                        img.getImageUrl(),
                        img.getType(),
                        img.getSortOrder()
                ))
                .toList();
    }
}
