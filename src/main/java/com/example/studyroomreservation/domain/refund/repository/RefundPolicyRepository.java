package com.example.studyroomreservation.domain.refund.repository;

import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    Page<RefundPolicy> findByIsActive(boolean active, Pageable pageable);
    boolean existsByName(String name);

}
