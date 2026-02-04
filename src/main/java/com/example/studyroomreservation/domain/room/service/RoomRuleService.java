package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.request.RoomRuleCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.RoomRulePickItemResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import com.example.studyroomreservation.domain.room.mapper.RoomRuleMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomRuleService {
    private final RoomRuleRepository roomRuleRepository;
    private final RoomRuleMapper roomRuleMapper;
    private final int PAGE_SIZE = 10;
    private final RoomRepository roomRepository;

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

    // 상태변경
    @Transactional
    public void updateStatus(Long id, boolean active) {
        RoomRule roomRule = roomRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "해당 규칙을 찾을 수 없습니다. ID: " + id));

        // 비활성화 시도시
        if (!active) {
            // 최소 1개 활성 조건
            if (roomRuleRepository.countByIsActiveTrue() <= 1) {
                throw new BusinessException(ErrorCode.RR_INVALID_REQUEST);
            }

            // Room에서 사용 중인지
            // [간단 테스트용], 도메인 완성시 아래 줄 사용
            boolean isUsedInRoom = false;
            // boolean isUsedInRoom = roomRepository.existsByRoomRuleId(id);
            if (isUsedInRoom) {
                throw new BusinessException(ErrorCode.RR_IN_USE);
            }
        }
        roomRule.toggleActiveStatus(active);
    }

    // 룸 등록시 정책 목록 조회용
    public List<RoomRulePickItemResponse> getActivePickItems() {
        return roomRuleRepository.findActivePickItems();
    }
}
