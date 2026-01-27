package com.example.studyroomreservation.domain.payment.repository;

import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
    boolean existsByReservationId(Long reservationId);
}
