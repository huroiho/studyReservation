package com.example.studyroomreservation.domain.refund.repository;

import com.example.studyroomreservation.domain.refund.entity.RefundRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRuleRepository extends JpaRepository<RefundRule, Long> {
}
