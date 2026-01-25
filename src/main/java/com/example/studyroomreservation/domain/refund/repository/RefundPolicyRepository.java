package com.example.studyroomreservation.domain.refund.repository;

import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {
}
