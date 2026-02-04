package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BaseSoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Table(name="rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseSoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operation_policy_id", nullable = false)
    private OperationPolicy operationPolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_rule_id", nullable = false)
    private RoomRule roomRule;

    @Column(name="refund_policy_id", nullable = false)
    private Long refundPolicyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name="max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer price;

    @JdbcTypeCode(SqlTypes.JSON) // TODO : @Convert로 바꿀지 논의
    @Column(name = "amenities", columnDefinition = "json")
    private Set<AmenityType> amenities = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status;

    @BatchSize(size = 100)
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomImage> images = new ArrayList<>();

    public enum RoomStatus{
        ACTIVE, INACTIVE
    }

    public enum AmenityType {
        WIFI, WHITEBOARD, PROJECTOR, AIR_CONDITIONER, COFFEE_MACHINE, SOUND_SYSTEM
    }

    private Room(OperationPolicy operationPolicy,
                 RoomRule roomRule,
                 Long refundPolicyId,
                 String name,
                 Integer maxCapacity,
                 Integer price,
                 Set<AmenityType> amenities,
                 RoomStatus status
    ){
        this.operationPolicy = operationPolicy;
        this.roomRule = roomRule;
        this.refundPolicyId = refundPolicyId;
        this.name = name;
        this.maxCapacity = maxCapacity;
        this.price = price;
        this.amenities = (amenities != null) ? new HashSet<>(amenities) : new HashSet<>();
        this.status = status;
    }

    // --- 정적 팩토리 메서드 ----
    public static Room create(OperationPolicy operationPolicy,
                              RoomRule roomRule,
                              Long refundPolicyId,
                              String name,
                              Integer maxCapacity,
                              Integer price,
                              Set<AmenityType> amenities) {

        return new Room(operationPolicy, roomRule, refundPolicyId, name, maxCapacity, price, amenities, RoomStatus.ACTIVE);
    }

    public void addImage(RoomImage image) {
        this.images.add(image);
        if (image.getRoom() != this) {
            image.initRoom(this);
        }
    }

    public void inactivate() {
        this.status = RoomStatus.INACTIVE;
    }

    public void activate() {
        this.status = RoomStatus.ACTIVE;
    }

    public void updateRoom(String name, Integer price, Integer maxCapacity) {
        this.name = name;
        this.price = price;
        this.maxCapacity = maxCapacity;
    }

    public String getThumbnailUrl() {
        if (this.images == null || this.images.isEmpty()) {
            return null;
        }
        return this.images.stream()
                .filter(img -> img.getType() == RoomImage.ImageType.THUMBNAIL)
                .findFirst()
                .map(RoomImage::getImageUrl)
                .orElse(null);
    }
}
