package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.refund.service.RefundPolicyQueryService;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* 공용 query 서비스 -> 다른 도메인이 Room 조회할 때 쓰는 용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomQueryService {

    private final RoomRepository roomRepository;

    public Room getById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    public List<Room> findByOperationPolicyId(Long policyId) {
        return roomRepository.findByOperationPolicyId(policyId);
    }

    public boolean existsByOperationPolicyId(Long policyId) {
        return roomRepository.existsByOperationPolicyId(policyId);
    }
}
