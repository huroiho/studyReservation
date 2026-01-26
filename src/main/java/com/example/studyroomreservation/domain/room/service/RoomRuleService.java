package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRuleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomRuleService {
    private final RoomRuleRepository roomRuleRepository;
    private final RoomMapper roomMapper;

    // 목록 조회
    public List<RoomRuleResponse> getAllRules() {
        List<RoomRule> rules = roomRuleRepository.findAllByOrderByIsActiveDescCreatedAtDesc();
        return roomMapper.toResponseList(rules);
    }

    // 활성화 목록 조회
    public List<RoomRuleResponse> getActiveRules() {
        List<RoomRule> rules = roomRuleRepository.findAllByIsActiveTrue();
        return roomMapper.toResponseList(rules);
    }

    // 상세 조회
    public RoomRuleResponse getRuleDetail(Long id) {
        RoomRule rule = roomRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 이용 규칙을 찾을 수 없습니다. ID: " + id));
        return roomMapper.toRuleResponse(rule);
    }
}
