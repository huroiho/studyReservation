package com.example.studyroomreservation.domain.refund.repository;

import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyListResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyPickItemResponse;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    Page<RefundPolicy> findByIsActive(boolean active, Pageable pageable);
    boolean existsByName(String name);

    @Query("SELECT p FROM RefundPolicy p LEFT JOIN FETCH p.rules WHERE p.id = :id")
    Optional<RefundPolicy> findByIdWithRules(@Param("id") Long id);

    @Query(
            value = """
            select new com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyListResponse(
                p.id,
                p.name,
                p.isActive,
                count(r.id),
                p.createdAt,
                p.activeUpdatedAt
            )
            from RefundPolicy p
            left join p.rules r
            where (:active is null or p.isActive = :active)
            group by p.id, p.name, p.isActive, p.createdAt, p.activeUpdatedAt
            order by p.id desc
        """,
            countQuery = """
            select count(p)
            from RefundPolicy p
            where (:active is null or p.isActive = :active)
        """
    )
    Page<RefundPolicyListResponse> findPolicyPageWithRuleCount(
            @Param("active") Boolean active,
            Pageable pageable
    );

    // 룸 등록에서 보여줄 목록용
    @Query("""
            SELECT new com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyPickItemResponse(
                p.id,
                p.name
            )
            FROM RefundPolicy p
            WHERE p.isActive = true
            ORDER BY p.id desc
        """)
    List<RefundPolicyPickItemResponse> findActivePickItems();
}
