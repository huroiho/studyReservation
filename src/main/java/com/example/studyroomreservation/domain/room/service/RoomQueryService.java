package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomQueryService {

    private final RoomRepository roomRepository;

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> findByOperationPolicyId(Long policyId) {
        return roomRepository.findByOperationPolicyId(policyId);
    }

    public boolean existsByOperationPolicyId(Long policyId) {
        return roomRepository.existsByOperationPolicyId(policyId);
    }
}
