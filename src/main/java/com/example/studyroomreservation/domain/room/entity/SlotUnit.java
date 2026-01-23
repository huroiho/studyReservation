package com.example.studyroomreservation.domain.room.entity;

import lombok.Getter;

@Getter
public enum SlotUnit {
    MINUTES_30(30),
    MINUTES_60(60);

    private final int minutes;

    SlotUnit(int minutes) {
        this.minutes = minutes;
    }
}
