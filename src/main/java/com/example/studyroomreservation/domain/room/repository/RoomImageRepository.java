package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    // 룸당 이미지 순서대로 조회
    List<RoomImage> findAllByRoomIdOrderBySortOrderAsc(Long roomId);
}
