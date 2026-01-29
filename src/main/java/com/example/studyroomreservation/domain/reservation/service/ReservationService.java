package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    private static final LocalDateTime BASIC_EXPIRED_AT = LocalDateTime.now().plusMinutes(10);
    /*
    방id랑 시작-종료시간 프론트에서 받고 컨트롤러에서 멤버 id 받아서

    이부분을 동시성을 위해 redis 분산락으로 코드 구성
    해당 룸 조회하고 거기에 체이닝으로 운영정책의 스케줄 리스트에 맞는지 확인하고
    해당 시간에 예약이 있는지 확인하고 -> 이거 조회 request로 받은 시간에 있는 예약이 존재하는지 확인하고

    이제 예약 생성하고

    예약 id 반환
    */

    @Transactional
    public Long creatReservation(ReservationCreateRequest request, Long memberId){

        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        OperationPolicy operationPolicy = room.getOperationPolicy();
        RoomRule roomRule = room.getRoomRule();

        // 운영 시간 검증

        // 최소시간, 예약 가능 기간 검증
        validateRoomRule(roomRule, request.startTime(), request.endTime());



        int totalAmount = calculateTotalAmount(room, request.startTime(), request.endTime());
        Reservation reservation = reservationMapper.toTempReservation(request, memberId, room, totalAmount,BASIC_EXPIRED_AT);
        reservationRepository.save(reservation);

        return reservation.getId();
    }




    // 편의 매서드
    private int calculateTotalAmount(Room room, LocalDateTime start, LocalDateTime end){
        long minutes = Duration.between(start, end).toMinutes();
        double hours = minutes / 60.0;
        return (int) (hours * room.getPrice());
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


}
