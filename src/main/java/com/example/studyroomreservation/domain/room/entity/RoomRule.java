package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BasePolicyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name="room_rules")
@NoArgsConstructor
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
    public static RoomRule create(String name, Integer minDurationMinutes, Integer bookingOpenDays){
        return new RoomRule(name, minDurationMinutes, bookingOpenDays);
    }
}
