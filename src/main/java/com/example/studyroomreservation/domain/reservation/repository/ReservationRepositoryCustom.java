package com.example.studyroomreservation.domain.reservation.repository;

import com.example.studyroomreservation.domain.reservation.entity.Reservation;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {

    boolean existsActiveReservation(Long roomId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findActiveReservations(Long roomId, LocalDateTime start, LocalDateTime end);

    // 마이페이지 예약현황
    List<Reservation> findMyActiveReservations(Long memberId, LocalDateTime now);
}
