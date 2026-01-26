package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.RoomRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRuleRepository extends JpaRepository<RoomRule, Long> {

    // 목록 조회 : 활성화/비활성화 -> 최신순 정렬
    List<RoomRule> findAllByOrderByIsActiveDescCreatedAtDesc();

    // 활성화 목록 조회 (룸 등록시 목록)
    List<RoomRule> findAllByIsActiveTrue();
}
