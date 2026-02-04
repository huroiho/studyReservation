package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.dto.response.RoomRulePickItemResponse;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomRuleRepository extends JpaRepository<RoomRule, Long> {

    // 목록 조회
    Page<RoomRule> findAll(Pageable pageable);

    // 활성화 목록 조회 (룸 등록시 목록 - 페이징)
    Page<RoomRule> findAllByIsActiveTrue(Pageable pageable);

    // 규칙명 중복 확인 (Validator에서 사용)
    boolean existsByName(String name);

    // 설정 값 조합 중복 체크 (최소 이용 시간 + 예약 가능 기간)
    boolean existsByMinDurationMinutesAndBookingOpenDays(Integer minDurationMinutes, Integer bookingOpenDays);

    // 현재 활성화된 규칙의 총 개수
    long countByIsActiveTrue();

    // 룸 등록에서 보여줄 목록용
    @Query("""
        SELECT new com.example.studyroomreservation.domain.room.dto.response.RoomRulePickItemResponse(
            r.id,
            r.name
        )
        FROM RoomRule r
        WHERE r.isActive = true
        ORDER BY r.id desc
    """)
    List<RoomRulePickItemResponse> findActivePickItems();
}
