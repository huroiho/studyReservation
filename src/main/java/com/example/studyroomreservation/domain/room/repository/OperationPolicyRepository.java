package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationPolicyRepository extends JpaRepository<OperationPolicy, Long> {

    boolean existsByName(String name);

    @Query("""
    select new com.example.studyroomreservation.domain.room.dto.response.OperationPolicyListResponse(
        p.id,
        p.name,
        p.slotUnit,
        p.isActive,
        p.createdAt,
        p.activeUpdatedAt
    )
    from OperationPolicy p
    order by p.id desc
"""    )
    Page<OperationPolicyListResponse> findList(Pageable pageable);

    @Query("""
        SELECT p FROM OperationPolicy p
        LEFT JOIN FETCH p.schedules
        WHERE p.id = :id
    """)
    Optional<OperationPolicy> findByIdWithSchedules(@Param("id") Long id);
}
