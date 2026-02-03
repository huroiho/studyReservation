package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyListResponse;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyPickItemResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OperationPolicyRepository extends JpaRepository<OperationPolicy, Long> {

    boolean existsByName(String name);

    /**
     * 활성화된 운영 정책 목록 조회 (룸 생성 폼용)
     */
    List<OperationPolicy> findAllByIsActiveTrue();

    /**
     * 스케줄과 함께 정책 조회 (상세 페이지용)
     */
    @Query("SELECT p FROM OperationPolicy p LEFT JOIN FETCH p.schedules WHERE p.id = :id")
    Optional<OperationPolicy> findByIdWithSchedules(@Param("id") Long id);

    /**
     * 목록 조회 (검색/필터/페이징 지원)
     * - roomCount는 서브쿼리로 계산
     */
    @Query("""
        SELECT new com.example.studyroomreservation.domain.room.dto.response.OperationPolicyListResponse(
            p.id,
            p.name,
            p.slotUnit,
            p.isActive,
            p.createdAt,
            p.activeUpdatedAt,
            (SELECT COUNT(r) FROM Room r WHERE r.operationPolicy = p AND r.deletedAt IS NULL)
        )
        FROM OperationPolicy p
        WHERE (:keyword IS NULL OR p.name LIKE %:keyword%)
          AND (:isActive IS NULL OR p.isActive = :isActive)
        ORDER BY p.id DESC
    """)
    Page<OperationPolicyListResponse> findList(
            @Param("keyword") String keyword,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );

    // 룸 등록에서 보여줄 목록용
    @Query("""
        SELECT new com.example.studyroomreservation.domain.room.dto.response.OperationPolicyPickItemResponse(
            p.id,
            p.name
        )
        FROM OperationPolicy p
        WHERE p.isActive = true
        ORDER BY p.id desc
    """)
    List<OperationPolicyPickItemResponse> findActivePickItems();
}
