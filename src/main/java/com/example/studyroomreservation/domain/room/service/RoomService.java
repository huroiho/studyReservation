package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservableTimeResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.domain.room.dto.response.RoomSlotResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.OperationSchedule;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.SlotUnit;
import com.example.studyroomreservation.domain.room.mapper.OperationPolicyMapper;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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
    private final OperationPolicyMapper operationPolicyMapper;
    private final ReservationService reservationService;

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

    public List<RoomSlotResponse> getRoomSlots(Long roomId, LocalDate date) {
        // date가 현재날짜보다 이전이면 예약 불가능
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.ROOM_INVALID_PAST_DATE);
        }

        // 룸+운영정책(스케줄 포함)
        Room room = roomRepository.findWithOperationPolicyById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != Room.RoomStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        OperationPolicy policy = room.getOperationPolicy();

        // Find schedule for the given day
        OperationSchedule schedule = policy.getSchedules().stream()
                .filter(s -> s.getDayOfWeek() == date.getDayOfWeek())
                .findFirst()
                .orElse(null);

        // If no schedule or closed day, return empty list
        if (schedule == null || schedule.isClosed()) {
            return List.of();
        }

        // Get reserved time ranges for the day (fact from reservation domain)
        List<RoomReservableTimeResponse> reservedTimes = reservationService.getReservedTimes(roomId, date);

        // Generate slots based on slotUnit
        SlotUnit slotUnit = policy.getSlotUnit();
        int slotMinutes = slotUnit.getMinutes();
        LocalTime openTime = schedule.getOpenTime();
        LocalTime closeTime = schedule.getCloseTime();

        List<RoomSlotResponse> slots = new ArrayList<>();
        LocalTime current = openTime;

        // 오늘 날짜인 경우 현재 시간 기준으로 지난 슬롯 판단
        boolean isToday = date.equals(LocalDate.now());
        LocalTime now = LocalTime.now();

        while (current.plusMinutes(slotMinutes).compareTo(closeTime) <= 0) {
            LocalTime slotEnd = current.plusMinutes(slotMinutes);
            LocalDateTime slotStartDt = date.atTime(current);
            LocalDateTime slotEndDt = date.atTime(slotEnd);

            // 슬롯 상태 결정: UNAVAILABLE > RESERVED > AVAILABLE
            RoomSlotResponse.SlotStatus status;

            if (isToday && current.isBefore(now)) {
                // 오늘이고 슬롯 종료 시간이 현재 시간보다 이전이면 예약 불가
                status = RoomSlotResponse.SlotStatus.UNAVAILABLE;
            } else {
                // Check if slot overlaps with any reserved time
                boolean isReserved = reservedTimes.stream()
                        .anyMatch(r -> r.startTime().isBefore(slotEndDt) && r.endTime().isAfter(slotStartDt));

                status = isReserved
                        ? RoomSlotResponse.SlotStatus.RESERVED
                        : RoomSlotResponse.SlotStatus.AVAILABLE;
            }

            slots.add(new RoomSlotResponse(current, slotEnd, status));
            current = slotEnd;
        }

        return slots;
    }
}
