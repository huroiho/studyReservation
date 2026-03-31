package com.example.studyroomreservation.domain.room.entity;

import lombok.Getter;

@Getter
public enum SlotUnit {
    MINUTES_30(30, "30분"),
    MINUTES_60(60, "60분");

    private final int minutes;
    private final String label;

    SlotUnit(int minutes, String label) {
        this.minutes = minutes;
        this.label = label;
    }
}
