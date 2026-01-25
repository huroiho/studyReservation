package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@Table(name = "operation_schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationSchedule extends BaseCreatedEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_policy_id", nullable = false)
    private OperationPolicy operationPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, updatable = false, length = 10)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_closed", nullable = false)
    private boolean isClosed = false;


    private OperationSchedule(OperationPolicy policy,
                              DayOfWeek dayOfWeek,
                              LocalTime openTime,
                              LocalTime closeTime,
                              boolean closed) {

        if (policy == null) throw new IllegalArgumentException("operationPolicy는 필수입니다.");
        if (dayOfWeek == null) throw new IllegalArgumentException("dayOfWeek는 필수입니다.");

        this.operationPolicy = policy;
        this.dayOfWeek = dayOfWeek;
        assignBusinessHours(openTime, closeTime, closed);
    }

    public static OperationSchedule create(OperationPolicy policy,
                                           DayOfWeek dayOfWeek,
                                           LocalTime openTime,
                                           LocalTime closeTime,
                                           boolean closed){
        return new OperationSchedule(policy,dayOfWeek,openTime,closeTime,closed);
    }

    private void assignBusinessHours(LocalTime openTime, LocalTime closeTime, boolean closed) {
        if(closed) {
            this.openTime = null;
            this.closeTime = null;
            return;
        }
        if (openTime == null || closeTime == null) {
            throw new IllegalArgumentException("운영일의 openTime/closeTime은 필수입니다.");
        }
        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("openTime은 closeTime보다 빨라야 합니다.");
        }
        this.openTime = openTime;
        this.closeTime = closeTime;
    }
}
