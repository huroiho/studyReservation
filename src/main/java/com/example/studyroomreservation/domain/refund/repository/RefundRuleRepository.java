package com.example.studyroomreservation.domain.refund.repository;

import com.example.studyroomreservation.domain.refund.entity.RefundRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefundRuleRepository extends JpaRepository<RefundRule, Long> {
    @Query("""
                select count(r)
                from RefundRule r
                where r.refundPolicy.id = :refundPolicyId
       """)
    long countByRefundPolicyId(@Param("refundPolicyId") Long refundPolicyId);
}
