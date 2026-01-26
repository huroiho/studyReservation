package com.example.studyroomreservation.domain.reservation.entity;

public enum ReservationStatus {
    TEMP,
    CONFIRMED,
    EXPIRED,
    CANCELED,
    USED;

    public boolean canChangeTo(ReservationStatus target) {
        return switch (this) {
            case TEMP -> target == CONFIRMED || target == CANCELED || target == EXPIRED;
            case CONFIRMED -> target == CANCELED || target == USED;
            default -> false;
        };
    }
}
