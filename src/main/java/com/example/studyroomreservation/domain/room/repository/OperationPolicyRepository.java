package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyListResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
