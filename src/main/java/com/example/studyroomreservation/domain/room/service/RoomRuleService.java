package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.request.RoomRuleCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import com.example.studyroomreservation.domain.room.mapper.RoomRuleMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRuleRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomRuleService {
    private final RoomRuleRepository roomRuleRepository;
    private final RoomRuleMapper roomRuleMapper;
    private final int PAGE_SIZE = 10;

    // 목록 조회
    public Page<RoomRuleResponse> getAllRoomRules(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        return roomRuleRepository.findAll(pageable)
                .map(roomRuleMapper::toRuleResponse);
    }

    // 활성화 목록 조회 (룸 등록시)
    public Page<RoomRuleResponse> getActiveRoomRules(int page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        return roomRuleRepository.findAllByIsActiveTrue(pageable)
                .map(roomRuleMapper::toRuleResponse);
    }

    // 상세 조회
    public RoomRuleResponse getRuleDetail(Long id) {
        return roomRuleRepository.findById(id)
                .map(roomRuleMapper::toRuleResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "해당 화면을 찾을 수 없습니다. ID: " + id));
    }

    //등록
    @Transactional
    public void createRoomRule(RoomRuleCreateRequest request) {
        RoomRule roomRule = roomRuleMapper.createRoomRule(request);
        roomRuleRepository.save(roomRule);
    }
}
