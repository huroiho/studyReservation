package com.example.studyroomreservation.domain.payment.repository;

import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    @Query("""
    select pa
    from PaymentAttempt pa
    where pa.reservationId = :reservationId
      and pa.paymentAttemptStatus = 'PENDING'
    order by pa.createdAt desc
""")
    Optional<PaymentAttempt> findLatestPendingByReservationId(Long reservationId);
}
