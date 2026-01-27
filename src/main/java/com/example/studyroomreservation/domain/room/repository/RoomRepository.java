package com.example.studyroomreservation.domain.room.repository;

import com.example.studyroomreservation.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

}
