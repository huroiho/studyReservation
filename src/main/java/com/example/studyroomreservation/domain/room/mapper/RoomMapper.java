package com.example.studyroomreservation.domain.room.mapper;

import com.example.studyroomreservation.domain.room.dto.response.RoomResponse;
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

    private Optional<RoomImage> findByType(List<RoomImage> images, RoomImage.ImageType type) {
        return images.stream()
                .filter(img -> img.getType() == type)
                .min(Comparator.comparing(RoomImage::getSortOrder));
    }
}
