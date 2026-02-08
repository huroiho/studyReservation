package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public boolean existsByAppliedOperationPolicyId(Long policyId) {
        return reservationRepository.existsByAppliedOperationPolicyId(policyId);
    }
}
