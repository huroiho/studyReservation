package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservableTimeResponse;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.mapper.ReservationMapper;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.OperationSchedule;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    private static final int BASIC_EXPIRED_TIME = 10;

    /*
    방id랑 시작-종료시간 프론트에서 받고 컨트롤러에서 멤버 id 받아서

    이부분을 동시성을 위해 redis 분산락으로 코드 구성
    해당 룸 조회하고  운영정책이랑 룸 규칙 맞는지 검증하고
    해당 시간에 예약이 있는지 확인하고
    이제 예약 생성
    */

    @Transactional
    public Long createReservation(ReservationCreateRequest request, Long memberId){

        if(request.startTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.RES_PAST_TIME_NOT_ALLOWED);

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        OperationPolicy operationPolicy = room.getOperationPolicy();
        RoomRule roomRule = room.getRoomRule();

        // 운영 시간 검증
        validateOperationSchedule(operationPolicy, request.startTime(), request.endTime());

        // 최소시간, 예약 가능 기간 검증
        validateRoomRule(roomRule, request.startTime(), request.endTime());

        //중복 예약 확인 QueryDSl 작성
        if (reservationRepository.existsActiveReservation(room.getId(), request.startTime(), request.endTime())) {
            throw new BusinessException(ErrorCode.RES_ALREADY_RESERVED);
        }

        int totalAmount = calculateTotalAmount(room, request.startTime(), request.endTime());

        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(BASIC_EXPIRED_TIME);

        Reservation reservation = reservationMapper.toTempReservation(request, memberId, room, totalAmount, expiredAt);
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    // RoomService에서 호출
    // TODO: RoomReservableTimeResponse 명칭 재검토 필요
    @Transactional(readOnly = true)
    public List<RoomReservableTimeResponse> getReservedTimes(Long roomId, LocalDate date){
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return reservationRepository.findActiveReservations(roomId, startOfDay, endOfDay);
    }

    private void validateRoomRule(RoomRule rule, LocalDateTime start, LocalDateTime end) {
        long duration = Duration.between(start, end).toMinutes();
        if (duration < rule.getMinDurationMinutes()) {
            throw new BusinessException(ErrorCode.RES_MIN_DURATION_NOT_MET);
        }

        LocalDate maxDate = LocalDate.now().plusDays(rule.getBookingOpenDays());
        if (start.toLocalDate().isAfter(maxDate)) {
            throw new BusinessException(ErrorCode.RES_BOOKING_PERIOD_EXCEEDED);
        }
    }


    // 편의 매서드
    private int calculateTotalAmount(Room room, LocalDateTime start, LocalDateTime end){
        long minutes = Duration.between(start, end).toMinutes();
        double hours = minutes / 60.0;
        return (int) (hours * room.getPrice());
    }

    private void validateOperationSchedule(OperationPolicy policy, LocalDateTime start, LocalDateTime end) {
        DayOfWeek dayOfWeek = start.getDayOfWeek();

        // 해당 요일의 스케줄 찾기
        OperationSchedule schedule = policy.getSchedules().stream()
                .filter(s -> s.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.OS_DAY_NOT_FOUND));

        // 1) 휴무일 체크
        if (schedule.isClosed()) {
            throw new BusinessException(ErrorCode.OS_CLOSED_DAY);
        }

        // 2) 오픈/마감 시간 체크
        LocalTime openTime = schedule.getOpenTime();
        LocalTime closeTime = schedule.getCloseTime();
        LocalTime reqStartTime = start.toLocalTime();
        LocalTime reqEndTime = end.toLocalTime();

        // TODO 당일 운영시간 기준, 금일 - 익일 동시 예약은 현재로서는 불가능
        if (reqStartTime.isBefore(openTime) || reqEndTime.isAfter(closeTime)) {
            throw new BusinessException(
                    ErrorCode.RES_OUT_OF_OPERATION_HOURS,
                    String.format("운영 시간: %s ~ %s", openTime, closeTime)
            );
        }

        // 하루 단위 예약인지 확인
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "예약은 하루 단위로만 가능합니다.");
        }
    }




}
