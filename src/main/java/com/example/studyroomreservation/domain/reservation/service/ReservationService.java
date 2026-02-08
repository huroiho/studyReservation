package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.service.MemberQueryService;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.service.PaymentQueryService;
import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.dto.response.AdminReservationResponse;
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
import com.example.studyroomreservation.domain.room.service.RoomQueryService;
import com.example.studyroomreservation.global.aop.DistributedLock;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationMapper reservationMapper;
    private final RoomQueryService roomQueryService;
    private final ReservationRepository reservationRepository;
    private final MemberQueryService memberQueryService;
    private final PaymentQueryService paymentQueryService;

    private static final int BASIC_EXPIRED_TIME = 10;

/*
    예약 생성 lock:reservation:room: - 네임스페이스(문자열, 키 값의 유일성을 위해 삽입)
    #request.roomId() 변수에서 방 번호 추출
    #request.startTime() 변수에서 시작 시간 추출
    ':' - 구분자 역할 추후 이 키값을 다시 코드에서 읽어들일 때 유용함
*/

    @DistributedLock(
            key = "'lock:reservation:room:' + #request.roomId() + ':' + #request.startTime().toLocalDate()",
            waitTime = 5L,
            leaseTime = -1 //와치독 활성화
    )
    //TODO: FACADE 패턴 적용해서 락 이후의 트랜잭션 로직 최소화 하기
    @Transactional
    public Long createReservation(ReservationCreateRequest request, Long memberId){

        if(request.startTime().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.RES_PAST_TIME_NOT_ALLOWED);

        Room room = roomQueryService.getById(request.roomId());

        OperationPolicy operationPolicy = room.getOperationPolicy();
        RoomRule roomRule = room.getRoomRule();

        // 운영 시간 검증
        operationPolicy.validateWithinOperatingHours(request.startTime(), request.endTime());

        // 최소시간, 예약 가능 기간 검증
        roomRule.validateReservable(request.startTime(), request.endTime(), LocalDate.now());

        //중복 예약 확인 QueryDSl 작성
        if (reservationRepository.existsActiveReservation(room.getId(), request.startTime(), request.endTime())) {
            throw new BusinessException(ErrorCode.RES_ALREADY_RESERVED);
        }

        int totalAmount = room.calculatePriceFor(request.startTime(), request.endTime());

        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(BASIC_EXPIRED_TIME);

        Reservation reservation = reservationMapper.toTempReservation(request, memberId, room, totalAmount, expiredAt);
        reservationRepository.save(reservation);

        return reservation.getId();
    }

    //예약 상세 조회
    public ReservationDetailResponse getReservationDetail(Long reservationId, Long memberId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (!reservation.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "본인의 예약만 조회할 수 있습니다.");
        }

        Room room = roomQueryService.getById(reservation.getRoomId());

        Member member = memberQueryService.getById(memberId);

        // 결제 정보는 없을 수도 있음 (결제 전 취소)
        Payment payment = paymentQueryService.findPaymentByReservationId(reservationId)
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
        );
    }

    public ReservationDetailResponse getReservationDetailForAdmin(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        Room room = roomQueryService.getById(reservation.getRoomId());

        Member member = memberQueryService.getById(reservation.getMemberId());

        Payment payment = paymentQueryService.findPaymentByReservationId(reservationId)
                .orElse(null);

        boolean isCancellable = reservation.isCancellable(LocalDateTime.now());

        return reservationMapper.toDetailResponse(
                reservation,
                member,
                payment,
                isCancellable,
                room
        );
    }

    // 마이페이지 예약 현황 조회
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
            throw new BusinessException(ErrorCode.RES_ALREADY_EXPIRED);
        }
    }

    // 관리자 용 예약 목록 조회(DTO 프로젝션 사용)
    @Transactional(readOnly = true)
    public Page<AdminReservationResponse> getAllReservationsForAdmin(Pageable pageable) {
        return reservationRepository.findAllReservationsForAdmin(pageable);
    }

    // 마이페이지 예약 히스토리 조회
    public Page<ReservationResponse> getMyReservationHistory(Long memberId, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        Page<Tuple> pageResults = reservationRepository.findMyReservationHistory(memberId, now, pageable);

        return pageResults.map(t -> reservationMapper.toResponse(t.get(reservation), t.get(room)));
    }
}
