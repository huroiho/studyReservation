package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BaseCreatedEntity;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
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
    private boolean isClosed;


    private OperationSchedule(OperationPolicy policy,
                              DayOfWeek dayOfWeek,
                              LocalTime openTime,
                              LocalTime closeTime,
                              boolean closed) {

        if (policy == null) throw new BusinessException(ErrorCode.OS_POLICY_REQUIRED);
        if (dayOfWeek == null) throw new BusinessException(ErrorCode.OS_DAY_REQUIRED);

        this.operationPolicy = policy;
        this.dayOfWeek = dayOfWeek;
        this.isClosed = closed;

        if (closed) {
            this.openTime = null;
            this.closeTime = null;
        } else {
            this.openTime = openTime;
            this.closeTime = closeTime;
        }
    }

    static OperationSchedule create(OperationPolicy policy,
                                    DayOfWeek dayOfWeek,
                                    LocalTime openTime,
                                    LocalTime closeTime,
                                    boolean closed){
        return new OperationSchedule(policy,dayOfWeek,openTime,closeTime,closed);
    }
}
