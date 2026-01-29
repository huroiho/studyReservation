package com.example.studyroomreservation.domain.payment.repository;

import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {

    @Query("""
        select a
        from PaymentAttempt a
        where a.orderId = :orderId
    """)
    Optional<PaymentAttempt> findByOrderIdForUpdate(@Param("orderId") String orderId); //db 락 용도
    Optional<PaymentAttempt> findByOrderId(String orderId);
    boolean existsByReservationId(Long reservationId);
}
