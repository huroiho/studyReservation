package com.example.studyroomreservation.domain.payment.repository;

import com.example.studyroomreservation.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
