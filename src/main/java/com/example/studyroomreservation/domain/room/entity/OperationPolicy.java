package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BasePolicyEntity;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "operation_policies")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationPolicy extends BasePolicyEntity {

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "slot_unit", nullable = false, updatable = false)
    private SlotUnit slotUnit;

    //정책 하나에 여러 시간표(스케줄)를 List로 담기
    @OneToMany(mappedBy = "operationPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperationSchedule> schedules = new ArrayList<>();

    private OperationPolicy(String name, SlotUnit slotUnit) {
        this.name = name;
        this.slotUnit = slotUnit;
    }

    public static OperationPolicy createWith7Days(String name, SlotUnit slotUnit,
                                         List<ScheduleDraft> drafts) {
        if (name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.OP_POLICY_NAME_REQUIRED);
        }
        if (slotUnit == null) {
            throw new BusinessException(ErrorCode.OP_SLOT_UNIT_REQUIRED);
        }
        OperationPolicy policy = new OperationPolicy(name, slotUnit);
        policy.initializeSchedules(drafts);
        return policy;
    }

    //검증 후 DB객체 생성
    private void initializeSchedules(List<ScheduleDraft> drafts) {
        if (drafts == null) {
            throw new BusinessException(ErrorCode.OP_POLICY_SCHEDULE_REQUIRED);
        }
        if (drafts.size() != 7) {
            throw new BusinessException(ErrorCode.OP_POLICY_SCHEDULE_NOT_7DAYS, "size=" + drafts.size());
        }

        // 요일 중복, 누락 방지
        EnumSet<DayOfWeek> seen =  EnumSet.noneOf(DayOfWeek.class);
        this.schedules.clear();

        for (ScheduleDraft d : drafts) {
            if (d.dayOfWeek() == null) {
                throw new BusinessException(ErrorCode.OP_POLICY_DAY_REQUIRED);
            }
            if (!seen.add(d.dayOfWeek())) {
                throw new BusinessException(ErrorCode.OP_POLICY_DAY_DUPLICATED, "day=" + d.dayOfWeek());
            }

            validateScheduleByPolicyRule(d);

            this.schedules.add(OperationSchedule.create(
                    this,
                    d.dayOfWeek(),
                    d.openTime(),
                    d.closeTime(),
                    d.isClosed()
                    )
            );
        }
    }

    private void validateScheduleByPolicyRule(ScheduleDraft d) {
        if (d.isClosed()) {
            if (d.openTime() != null || d.closeTime() != null) {
                throw new BusinessException(
                        ErrorCode.OS_CLOSED_TIME_NOT_ALLOWED,
                        "day=" + d.dayOfWeek()
                );
            }
            return;
        }

        if (d.openTime() == null || d.closeTime() == null) {
            throw new BusinessException(ErrorCode.OS_TIME_REQUIRED, "day=" + d.dayOfWeek());
        }

        if (!isHourOnly(d.openTime()) || !isHourOnly(d.closeTime())) {
            throw new BusinessException(
                    ErrorCode.OS_HOUR_ONLY,
                    "day=" + d.dayOfWeek() + ", open=" + d.openTime() + ", close=" + d.closeTime()
            );
        }

        if (!d.openTime().isBefore(d.closeTime())) {
            throw new BusinessException(
                    ErrorCode.OS_TIME_ORDER_INVALID,
                    "day=" + d.dayOfWeek() + ", open=" + d.openTime() + ", close=" + d.closeTime()
            );
        }

        long minutes = Duration.between(d.openTime(), d.closeTime()).toMinutes();
        int unit = this.slotUnit.getMinutes();
        if (minutes % unit != 0) {
            throw new BusinessException(
                    ErrorCode.OS_NOT_ALIGNED_TO_SLOT,
                    "day=" + d.dayOfWeek() + ", minutes=" + minutes + ", unit=" + unit
            );
        }
    }

    private boolean isHourOnly(LocalTime t) {
        return t.getMinute() == 0 && t.getSecond() == 0 && t.getNano() == 0;
    }

    // --- 읽기 전용 ---
    public List<OperationSchedule> getSchedules() {
        return Collections.unmodifiableList(this.schedules);
    }

    // activate() / deactivate()는 BasePolicyEntity에서 상속
    // - 이미 활성/비활성 상태이면 no-op (idempotent)

    // draft로 묶어서 요일별 세트로 가져가기 위해.
    // 레코드(record)는 클래스 내부에 선언될 때 명시적으로 static을 붙이지 않아도 무조건 static으로 동작
    public record ScheduleDraft(
            DayOfWeek dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime,
            boolean isClosed
    ) {}

    //================================================
    public void validateWithinOperatingHours(LocalDateTime start, LocalDateTime end) {
        // 하루 단위 예약인지 확인
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "예약은 하루 단위로만 가능합니다.");
        }

        DayOfWeek dayOfWeek = start.getDayOfWeek();

        OperationSchedule schedule = this.schedules.stream()
                .filter(s -> s.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.OS_DAY_NOT_FOUND));

        if (schedule.isClosed()) {
            throw new BusinessException(ErrorCode.OS_CLOSED_DAY);
        }

        LocalTime openTime = schedule.getOpenTime();
        LocalTime closeTime = schedule.getCloseTime();
        LocalTime reqStartTime = start.toLocalTime();
        LocalTime reqEndTime = end.toLocalTime();

        // TODO 당일 운영시간 기준, 금일 - 익일 동시 예약은 현재로서는 불가능
        if (reqStartTime.isBefore(openTime) || reqEndTime.isAfter(closeTime)) {
            throw new BusinessException(
                    ErrorCode.RES_OUT_OF_OPERATION_HOURS,
                    String.format("운영 시간: %s ~ %s", openTime, closeTime)
            );
        }
    }
}
