package com.example.studyroomreservation.domain.room.web;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.entity.SlotUnit;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class OperationPolicyFormFactory {

    public OperationPolicyCreateRequest emptyCreateForm() {
        List<OperationPolicyCreateRequest.ScheduleRequest> schedules = new ArrayList<>();
        for (DayOfWeek day : orderedDays()) {
            schedules.add(new OperationPolicyCreateRequest.ScheduleRequest(day, null, null, false));
        }
        return new OperationPolicyCreateRequest("", null, schedules);
    }

    public List<SlotUnit> slotUnits() {
        return Arrays.asList(SlotUnit.values());
    }

    public List<LocalTime> hourOptions() {
        List<LocalTime> list = new ArrayList<>();
        for (int h = 0; h <= 23; h++) list.add(LocalTime.of(h, 0));
        return list;
    }

    public List<DayOfWeek> orderedDays() {
        return List.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
                DayOfWeek.SATURDAY,
                DayOfWeek.SUNDAY
        );
    }
}