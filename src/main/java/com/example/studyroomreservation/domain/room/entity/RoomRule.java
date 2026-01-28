package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BasePolicyEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name="room_rules")
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용
//@AllArgsConstructor // MapStruct 주입용 (Mapper에서 찾아서 매핑)
public class RoomRule extends BasePolicyEntity {

    @Column(name="min_duration_minutes", nullable = false)
    private Integer minDurationMinutes;

    @Column(name="booking_open_days", nullable = false)
    private Integer bookingOpenDays;

    // --- Private 생성자  ---
    private RoomRule(String name, Integer minDurationMinutes, Integer bookingOpenDays){
        this.name = name;
        this.minDurationMinutes = minDurationMinutes != null ? minDurationMinutes : 60;
        this.bookingOpenDays = bookingOpenDays != null ? bookingOpenDays : 30;
    };

    // --- 정적 팩토리 메서드 ----
    public static RoomRule createRoomRule(String name, Integer minDurationMinutes, Integer bookingOpenDays, boolean active){
        RoomRule rule = new RoomRule(name, minDurationMinutes, bookingOpenDays);
        if (!active) rule.deactivate();
        return rule;
    }
}
