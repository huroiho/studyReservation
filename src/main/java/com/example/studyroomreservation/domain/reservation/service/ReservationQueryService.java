package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservedTimeResponse;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation getById(Long id){
        return findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
    }

    public boolean existsByAppliedOperationPolicyId(Long policyId) {
        return reservationRepository.existsByAppliedOperationPolicyId(policyId);
    }

    // RoomService에서 호출
    public List<RoomReservedTimeResponse> getReservedTimes(Long roomId, LocalDate date){
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return reservationRepository.findActiveReservations(roomId, startOfDay, endOfDay);
    }
}
