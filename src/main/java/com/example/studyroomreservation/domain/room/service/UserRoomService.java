package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservedTimeResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.domain.room.dto.response.RoomSlotResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.OperationSchedule;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
public class UserRoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final ReservationService reservationService;

    public Page<UserRoomListResponse> getUserList(Integer minCapacity, Pageable pageable) {

        // ID 기준으로 페이징/정렬 먼저
        Page<Long> idPage = roomRepository.findActiveRoomIds(minCapacity, pageable);

        if (idPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // 현재 페이지에 해당하는 ID 목록으로 목록용 DTO 조회
        List<Long> ids = idPage.getContent();
        List<UserRoomListResponse> responses = roomRepository.findUserListResponsesByIds(ids);

        // IN 조회 결과 순서 보정
        Map<Long, UserRoomListResponse> responseMap = responses.stream()
                .collect(Collectors.toMap(UserRoomListResponse::id, Function.identity()));
        List<UserRoomListResponse> items = ids.stream()
                .map(responseMap::get)
                .toList();

        return new PageImpl<>(items, pageable, idPage.getTotalElements());
    }

    public UserRoomDetailResponse getUserDetail(Long roomId) {
        Room room = roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != Room.RoomStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        return roomMapper.toUserDetailResponse(room);
    }

    // TODO : LocalDate.now()/LocalTime.now() -> clock 주입으로 수정
    public List<RoomSlotResponse> getRoomSlots(Long roomId, LocalDate date) {
        // date가 현재날짜보다 이전이면 예약 불가능
        if (date.isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.ROOM_INVALID_PAST_DATE);
        }

        Room room = roomRepository.findWithOperationPolicyById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != Room.RoomStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        OperationPolicy policy = room.getOperationPolicy();

        // 해당 날짜 스케줄 확인
        OperationSchedule schedule = policy.getSchedules().stream()
                .filter(s -> s.getDayOfWeek() == date.getDayOfWeek())
                .findFirst()
                .orElse(null);

        if (schedule == null || schedule.isClosed()) {
            return List.of();
        }

        // 예약된 시간 조회(예약 서비스 호출 - 순환 참조 없는거 확인)
        List<RoomReservedTimeResponse> reservedTimes = reservationService.getReservedTimes(roomId, date);

        return buildSlots(date, policy, schedule, reservedTimes);
    }

    // 슬롯 생성
    private List<RoomSlotResponse> buildSlots(
            LocalDate date,
            OperationPolicy policy,
            OperationSchedule schedule,
            List<RoomReservedTimeResponse> reservedTimes
    ) {
        int slotMinutes = policy.getSlotUnit().getMinutes();
        LocalTime openTime = schedule.getOpenTime();
        LocalTime closeTime = schedule.getCloseTime();

        boolean isToday = date.equals(LocalDate.now());
        LocalTime now = isToday ? LocalTime.now() : null;

        List<RoomSlotResponse> slots = new ArrayList<>();
        LocalTime current = openTime;

        while (current.plusMinutes(slotMinutes).compareTo(closeTime) <= 0) {
            LocalTime slotEnd = current.plusMinutes(slotMinutes);

            RoomSlotResponse.SlotStatus status =
                    determineSlotStatus(date, current, slotEnd, reservedTimes, isToday, now);

            slots.add(new RoomSlotResponse(current, slotEnd, status));
            current = slotEnd;
        }

        return slots;
    }

    // 슬롯 상태 판정
    private RoomSlotResponse.SlotStatus determineSlotStatus(
            LocalDate date,
            LocalTime slotStart,
            LocalTime slotEnd,
            List<RoomReservedTimeResponse> reservedTimes,
            boolean isToday,
            LocalTime now
    ) {
        if (isToday && slotStart.isBefore(now)) {
            return RoomSlotResponse.SlotStatus.UNAVAILABLE;
        }

        LocalDateTime slotStartDt = date.atTime(slotStart);
        LocalDateTime slotEndDt = date.atTime(slotEnd);

        // TODO : 슬롯 단위의 점유 여부(boolean)를 배열로 미리 계산하는 방법 고려
        boolean reserved = reservedTimes.stream()
                .anyMatch(r -> r.startTime().isBefore(slotEndDt) && r.endTime().isAfter(slotStartDt));

        return reserved
                ? RoomSlotResponse.SlotStatus.RESERVED
                : RoomSlotResponse.SlotStatus.AVAILABLE;
    }
}
