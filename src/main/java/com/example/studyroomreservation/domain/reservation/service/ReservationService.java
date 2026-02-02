package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationMapper reservationMapper;

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    private static final int BASIC_EXPIRED_TIME = 10;

    @Transactional
    public Long createReservation(ReservationCreateRequest request, Long memberId){

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

    // 예약 불가능 시간 조회
    @Transactional
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

    //예약 상세 조회
    @Transactional
    public ReservationDetailResponse getReservationDetail(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "본인의 예약만 조회할 수 있습니다.");
        }

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 결제 정보는 없을 수도 있음 (결제 전 취소)
        Payment payment = paymentRepository.findByReservationId(reservationId)
                .orElse(null);

        // 취소 가능 여부 계산
        boolean isCancellable = reservation.isCancellable(LocalDateTime.now());

        // 파라미터 순서: reservationInfo, roomInfo, memberInfo, paymentInfo, isReservationCancellable
        //TODO: 자주 나오는 인자 값들 공통으로 관리해서 코드 수 줄여보기
        //TODO: DTO 로 받아서 처리하기
        return reservationMapper.toDetailResponse(reservation, room, member, payment, isCancellable);
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
