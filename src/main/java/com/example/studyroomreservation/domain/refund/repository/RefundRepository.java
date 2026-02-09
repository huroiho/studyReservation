package com.example.studyroomreservation.domain.refund.repository;

import com.example.studyroomreservation.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
