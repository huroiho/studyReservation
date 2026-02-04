package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    // 룸 목록 조회용(id만 먼저 조회)
    @Query("""
        SELECT r.id FROM Room r
        WHERE r.status = 'ACTIVE' AND r.deletedAt IS NULL
          AND (:minCapacity IS NULL OR r.maxCapacity >= :minCapacity)
        """)
    Page<Long> findActiveRoomIds(@Param("minCapacity") Integer minCapacity, Pageable pageable);

    // 룸 목록 조회용(이미지에서 썸네일만 dto에 담아 조회)
    @Query("""
        SELECT new com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse(
            r.id, r.name, r.maxCapacity, r.price, i.imageUrl
        )
        FROM Room r
        LEFT JOIN RoomImage i ON i.room = r AND i.type = 'THUMBNAIL'
        WHERE r.id IN :ids
        """)
    List<UserRoomListResponse> findUserListResponsesByIds(@Param("ids") List<Long> ids);

    // 룸 상세 조회용
    @EntityGraph(attributePaths = {"images", "operationPolicy", "roomRule"})
    @Query("SELECT r FROM Room r WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Room> findUserDetailById(@Param("id") Long id);

    // 운영 정책을 사용 중인 룸 목록 조회
    @Query("SELECT r FROM Room r WHERE r.operationPolicy.id = :policyId AND r.deletedAt IS NULL")
    List<Room> findByOperationPolicyId(@Param("policyId") Long policyId);

    // 운영 정책을 사용 중인 룸 존재 여부 확인
    @Query("SELECT COUNT(r) > 0 FROM Room r WHERE r.operationPolicy.id = :policyId AND r.deletedAt IS NULL")
    boolean existsByOperationPolicyId(@Param("policyId") Long policyId);

    // 사용자 Room Slots용 (Room + OperationPolicy + schedules)
    @Query("SELECT r FROM Room r " +
            "JOIN FETCH r.operationPolicy op " +
            "LEFT JOIN FETCH op.schedules " +
            "WHERE r.id = :id AND r.deletedAt IS NULL")
    Optional<Room> findWithOperationPolicyById(@Param("id") Long id);

    @Query(
        value = """
        SELECT new com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse(
            r.id, r.name, r.maxCapacity, r.price, r.status, i.imageUrl
        )
        FROM Room r
        LEFT JOIN RoomImage i
            ON i.room = r AND i.type = 'THUMBNAIL'
        WHERE r.deletedAt IS NULL
        ORDER BY r.id DESC
    """,
    countQuery = """
        SELECT COUNT(r)
        FROM Room r
        WHERE r.deletedAt IS NULL
    """)
    Page<AdminRoomListResponse> findAdminRoomList(Pageable pageable);
}
