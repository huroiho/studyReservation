package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.mapper.OperationPolicyMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final OperationPolicyMapper operationPolicyMapper;

    // 예약 가능한 시간 조회용
    public OperationPolicyResponse getRoomPolicy(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        OperationPolicy policy = room.getOperationPolicy();

        return operationPolicyMapper.toOperationPolicyResponseForRoom(policy);
    }
}
