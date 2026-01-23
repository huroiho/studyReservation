package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BasePolicyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

        validate(name, minDurationMinutes, bookingOpenDays);
        return new RoomRule(name, minDurationMinutes, bookingOpenDays);
    }

    // --- 유효성 검증 ----
    private static void validate(
            String name,
            Integer minDurationMinutes,
            Integer bookingOpenDays
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("정책 이름은 필수입니다.");
        }
        if (minDurationMinutes != null && minDurationMinutes <= 0) {
            throw new IllegalArgumentException("최소 이용시간은 1분 이상이어야 합니다.");
        }
        if (bookingOpenDays != null && bookingOpenDays < 0) {
            throw new IllegalArgumentException("예약 오픈일 수는 0 이상이어야 합니다.");
        }
    }
}
