package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
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
}
