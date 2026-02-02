package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationResponse;
import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservedTimeResponse;
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
import com.querydsl.core.Tuple;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.studyroomreservation.domain.reservation.entity.QReservation.reservation;
import static com.example.studyroomreservation.domain.room.entity.QRoom.room;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationMapper reservationMapper;

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    private static final int BASIC_EXPIRED_TIME = 10;

    //TODO: 락 걸기
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
    @Transactional(readOnly = true)
    public List<RoomReservedTimeResponse> getReservedTimes(Long roomId, LocalDate date){
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
        return reservationMapper.toDetailResponse(
                reservation,
                member,
                payment,
                isCancellable,
                room
        );    }

    // 마이페이지 예약 현황 조회
    @Transactional
    public List<ReservationResponse> getMyActiveReservations(Long memberId) {
        List<Tuple> results = reservationRepository.findMyActiveReservationsWithRoom(memberId, LocalDateTime.now());
        return results.stream()
                .map(t -> {
                    Reservation res = t.get(reservation); // 예약 꺼내기
                    Room rm = t.get(room);               // 룸 꺼내기
                    return reservationMapper.toResponse(res, rm); // 룸 + 예약 합쳐 DTO 생성
                })
                .collect(Collectors.toList());
    }

    //예약 취소
    @Transactional
    public void cancelReservation(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        // 1. 본인 확인
        if (!reservation.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "본인의 예약만 취소할 수 있습니다.");
        }

        // 취소 처리: 현재 로직 -TEMP 상태일 때만 취소 가능 (CONFIRMED는 엔티티에서 막힘)
        reservation.cancel(LocalDateTime.now());

        /*TODO: 결제 취소: 결제 정보가 있다면 환불 처리
        paymentRepository.findByReservationId(reservationId)
        */
    }

    // 임시 예약 확인 -> 결제 전 후에 확인
    @Transactional
    public void confirmReservation(Long reservationId) {
        long result = reservationRepository.confirmIfTemp(reservationId, LocalDateTime.now());

        if (result == 0) {
            throw new BusinessException(ErrorCode.RES_EXPIRED);
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
