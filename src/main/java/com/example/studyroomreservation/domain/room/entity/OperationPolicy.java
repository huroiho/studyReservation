package com.example.studyroomreservation.domain.room.entity;

import com.example.studyroomreservation.global.common.BasePolicyEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationPolicy extends BasePolicyEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_unit", nullable = false, updatable = false)
    private SlotUnit slotUnit;

    //정책 하나에 여러 시간표, 스케줄을 List로 담기
    //orphanRemoval = 고아객체 제거
    @OneToMany(mappedBy = "operationPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OperationSchedule> schedules = new ArrayList<>();

    private OperationPolicy(String name, SlotUnit slotUnit) {
        this.name = name;
        this.slotUnit = slotUnit;
    }

    public static OperationPolicy createWith7Days(String name, SlotUnit slotUnit,
                                         List<ScheduleDraft> drafts) {
        OperationPolicy policy = new OperationPolicy(name, slotUnit);
        policy.initializeSchedules(drafts);
        return policy;
    }

    //검증 후 DB객체 생성
    private void initializeSchedules(List<ScheduleDraft> drafts) {
        if (drafts == null || drafts.size() != 7) {
            throw new IllegalArgumentException("월~일 7개의 OperationSchedule이 필요");
        }

        // 요일 중복, 누락 방지
        EnumSet<DayOfWeek> seen =  EnumSet.noneOf(DayOfWeek.class);

        //기존에 담긴 스케줄 비우기
        this.schedules.clear();

        for (ScheduleDraft d : drafts) {
            if (d.dayOfWeek() == null) throw new IllegalArgumentException("dayOfWeek는 필수입니다.");
            if (!seen.add(d.dayOfWeek())) {
                throw new IllegalStateException("요일 스케줄이 중복되었습니다: " + d.dayOfWeek());
            }
            OperationSchedule schedule = OperationSchedule.create(
                    this,
                    d.dayOfWeek(),
                    d.openTime(),
                    d.closeTime(),
                    d.isClosed()
            );
            this.schedules.add(schedule);
        }
        if (seen.size() != 7) {
            throw new IllegalStateException("OperationPolicy는 월~일 모든 요일 스케줄이 필요합니다.");
        }
    }

    // draft로 묶어서 요일별 세트로 가져가기 위해.
    // 레코드(record)는 클래스 내부에 선언될 때 명시적으로 static을 붙이지 않아도 무조건 static으로 동작
    public record ScheduleDraft(
            DayOfWeek dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime,
            boolean isClosed
    ) {}

}
