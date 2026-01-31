package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.response.UserRoomDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.mapper.OperationPolicyMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public Page<UserRoomListResponse> getUserList(Integer minCapacity, Pageable pageable) {
        Page<Long> idPage = roomRepository.findActiveRoomIds(minCapacity, pageable);

        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Room> rooms = roomRepository.findWithImagesByIds(idPage.getContent());

        Map<Long, Room> roomMap = rooms.stream()
                .collect(Collectors.toMap(Room::getId, Function.identity()));

        List<UserRoomListResponse> items = idPage.getContent().stream()
                .map(roomMap::get)
                .map(roomMapper::toUserListResponse)
                .toList();

        return new PageImpl<>(items, pageable, idPage.getTotalElements());
    }

    public UserRoomDetailResponse getUserDetail(Long roomId) {
        Room room = roomRepository.findUserDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != Room.RoomStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        return roomMapper.toUserDetailResponse(room);
    }

    // 예약 가능한 시간 조회용
    public OperationPolicyResponse getRoomPolicy(Long roomId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        OperationPolicy policy = room.getOperationPolicy();

        return operationPolicyMapper.toOperationPolicyResponseForRoom(policy);
    }
}
