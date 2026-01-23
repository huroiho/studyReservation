package com.example.studyroomreservation.domain.reservation.entity;

public enum ReservationStatus {
    TEMP,
    CONFIRMED,
    EXPIRED,
    CANCELED,
    USED

    // TEMP -> CONFIRMED || EXPIRED || CANCELED
    // CONFIRMED -> CANCELED || USED
    // EXPIRED, CANCELED, USED 종결

    public boolean canChangeTo(ReservationStatus target){
        // if 현재 = 타겟 상태면 아무일 없어야함
        // if target == temp -> CONFIRMED || EXPIRED || CANCELED면 return T 아니면 F
        // else if target == confirmed > CANCELED || USED면 return T 아니면 F
        // else return false;
    }
}
