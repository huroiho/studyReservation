package com.example.studyroomreservation.domain.reservation.repository;

import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservedTimeResponse;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {

    boolean existsActiveReservation(Long roomId, LocalDateTime start, LocalDateTime end);

    List<RoomReservedTimeResponse> findActiveReservations(Long roomId, LocalDateTime start, LocalDateTime end);

    // 마이페이지 예약현황
    List<Tuple> findMyActiveReservationsWithRoom(Long memberId, LocalDateTime now);

    Page<Tuple> findMyReservationHistory(Long memberId, LocalDateTime now, Pageable pageable);
}
