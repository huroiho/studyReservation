package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoomImage extends BaseCreatedEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="room_id", nullable = false)
    private Room room;

    @Column(name="image_url", nullable = false, length = 255)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ImageType type;

    @Column(name="sort_order",  nullable = false)
    private Integer sortOrder;


    public enum ImageType {
        MAIN, THUMBNAIL, GENERAL
    }

    // --- 정적 팩토리 메서드 ----
    public static RoomImage create(Room room, String imageUrl, ImageType type, Integer sortOrder){

        validate(room, imageUrl);

        RoomImage roomImage = new RoomImage();
        roomImage.room = room;
        roomImage.imageUrl = imageUrl;
        roomImage.type = type;
        roomImage.sortOrder = sortOrder;

        return roomImage;
    }
    // --- 유효성 검증 ----
    private static void validate(Room room, String imageUrl
    ) {
        if (room == null) {
            throw new IllegalArgumentException("Room은 필수입니다.");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("이미지는 필수입니다.");
        }
    }
}
