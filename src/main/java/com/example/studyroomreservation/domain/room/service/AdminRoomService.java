package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRoomService {

    private final RoomRepository roomRepository;

    public Page<AdminRoomListResponse> getAdminRoomList(Pageable pageable) {
        return roomRepository.findAdminRoomList(pageable);
    }
}
