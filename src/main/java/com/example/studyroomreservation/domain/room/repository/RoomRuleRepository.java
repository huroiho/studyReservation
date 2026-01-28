package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.RoomRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRuleRepository extends JpaRepository<RoomRule, Long> {

    // 목록 조회
    Page<RoomRule> findAll(Pageable pageable);

    // 활성화 목록 조회 (룸 등록시 목록)
    Page<RoomRule> findAllByIsActiveTrue(Pageable pageable);

    // 규칙명 중복 확인 (Validator에서 사용)
    boolean existsByName(String name);
}
