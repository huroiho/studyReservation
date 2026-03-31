package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.refund.service.RefundPolicyQueryService;
import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomUpdateResponse;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRoomQueryService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final RefundPolicyQueryService refundPolicyQueryService;

    public RoomUpdateResponse getRoomForEdit(Long roomId) {
        Room room = roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        String refundPolicyName = refundPolicyQueryService.getRefundPolicyName(room.getRefundPolicyId());
        return roomMapper.toRoomUpdateResponse(room, refundPolicyName);
    }

    public Page<AdminRoomListResponse> getAdminRoomList(Pageable pageable) {
        return roomRepository.findAdminRoomList(pageable);
    }

    public RoomDetailResponse getAdminRoomDetail(Long roomId) {
        Room room = roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        return roomMapper.toDetailResponse(room);
    }
}
