package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // ========== User Room List (two-query approach) ==========

    @Query("""
        SELECT r.id FROM Room r
        WHERE r.status = 'ACTIVE' AND r.deletedAt IS NULL
          AND (:minCapacity IS NULL OR r.maxCapacity >= :minCapacity)
        """)
    Page<Long> findActiveRoomIds(@Param("minCapacity") Integer minCapacity, Pageable pageable);

    @Query("""
        SELECT DISTINCT r FROM Room r
        LEFT JOIN FETCH r.images
        WHERE r.id IN :ids
        """)
    List<Room> findWithImagesByIds(@Param("ids") List<Long> ids);
    // 룸 상세조회시 이미지와 규칙 한 번에
    @Query("select r from Room r " +
            "left join fetch r.images " +
            "join fetch r.roomRule " +
            "where r.id = :id")
    Optional<Room> findByIdWithDetails(@Param("id") Long id);

    // 활성화된 룸 목록 조회 (사용자(관리자는 findAll()로 조회))
    List<Room> findAllByStatus(String status);

    // 해당 roomRuleId를 참조하는 Room 존재 확인
    boolean existsByRoomRuleId(Long roomRuleId);

    // 운영 정책을 사용 중인 룸 목록 조회
    @Query("SELECT r FROM Room r WHERE r.operationPolicy.id = :policyId AND r.deletedAt IS NULL")
    List<Room> findByOperationPolicyId(@Param("policyId") Long policyId);

    // 운영 정책을 사용 중인 룸 존재 여부 확인
    @Query("SELECT COUNT(r) > 0 FROM Room r WHERE r.operationPolicy.id = :policyId AND r.deletedAt IS NULL")
    boolean existsByOperationPolicyId(@Param("policyId") Long policyId);
}
