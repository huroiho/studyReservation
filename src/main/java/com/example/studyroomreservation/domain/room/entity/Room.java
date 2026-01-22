package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BaseSoftDeletableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseSoftDeletableEntity {
    @Column(name="operation_policy_id", nullable = false)
    private Long operationPolicyId;

    @Column(name="refund_policy_id", nullable = false)
    private Long refundPolicyId;

    @Column(name="room_policy_id", nullable = false)
    private Long roomPolicyId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name="max_capacity", nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer price;

    @Column(columnDefinition = "json")
    private String amenity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.ACTIVE;

    public enum RoomStatus{
        ACTIVE, INACTIVE
    }

    // --- 정적 팩토리 메서드 ----
    public static Room create(Long operationPolicyId,
                              Long refundPolicyId,
                              Long roomPolicyId,
                              String name,
                              Integer maxCapacity,
                              Integer price,
                              String amenity) {

        validate(operationPolicyId, refundPolicyId, roomPolicyId, name, maxCapacity, price);

        Room room = new Room();
        room.operationPolicyId = operationPolicyId;
        room.refundPolicyId = refundPolicyId;
        room.roomPolicyId = roomPolicyId;
        room.name = name;
        room.maxCapacity = maxCapacity;
        room.price = price;
        room.amenity = amenity;
        room.status = RoomStatus.ACTIVE;

        return room;
    }


    // --- 유효성 검증 ----
    private static void validate(
            Long operationPolicyId,
            Long refundPolicyId,
            Long roomPolicyId,
            String name,
            Integer maxCapacity,
            Integer price
    ) {
        if (operationPolicyId == null || refundPolicyId == null || roomPolicyId == null) {
            throw new IllegalArgumentException("정책 ID는 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("방 이름은 필수입니다.");
        }
        if (maxCapacity == null || maxCapacity <= 0) {
            throw new IllegalArgumentException("수용 인원은 1명 이상이어야 합니다.");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다.");
        }
    }
}
