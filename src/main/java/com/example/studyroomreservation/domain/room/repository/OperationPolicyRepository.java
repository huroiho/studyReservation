package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OperationPolicyRepository extends JpaRepository<OperationPolicy, Long> {

    boolean existsByName(String name);

    @Query("""
        SELECT p FROM OperationPolicy p
        LEFT JOIN FETCH p.schedules
        WHERE p.id = :id
    """)
    Optional<OperationPolicy> findByIdWithSchedules(@Param("id") Long id);
}
