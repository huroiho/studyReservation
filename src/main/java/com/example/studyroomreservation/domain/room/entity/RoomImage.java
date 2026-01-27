package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="room_images")
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

    private RoomImage(Room room, String imageUrl, ImageType type, Integer sortOrder){
        this.room = room;
        this.imageUrl = imageUrl;
        this.type = type;
        this.sortOrder = sortOrder;
    }

    // --- 정적 팩토리 메서드 ----
    public static RoomImage create(Room room, String imageUrl, ImageType type, Integer sortOrder){

        RoomImage roomImage = new RoomImage(room, imageUrl, type, sortOrder);
        room.addImage(roomImage);
        return roomImage;
    }

    //==========================================
    public void initRoom(Room room) {
        this.room = room;
    }

}
