package com.example.studyroomreservation.domain.reservation.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.service.MemberQueryService;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.payment.service.PaymentQueryService;
import com.example.studyroomreservation.domain.payment.service.PaymentService;
import com.example.studyroomreservation.domain.refund.dto.response.RefundCalculationResponse;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.entity.RefundRule;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.mapper.ReservationMapper;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import com.example.studyroomreservation.domain.room.entity.SlotUnit;
import com.example.studyroomreservation.domain.room.service.RoomQueryService;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    // --- 외부 의존성 Mock 처리 ---
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private RoomQueryService roomQueryService;
    @Mock
    private MemberQueryService memberQueryService;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RefundPolicyService refundPolicyService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ReservationTransactionHelper reservationTransactionHelper;
    @Mock
    private ReservationMapper reservationMapper;

    // --- 테스트용 실제 도메인 객체 ---
    private Room room;
    private Member member;
    private OperationPolicy operationPolicy;
    private RoomRule roomRule;
    private RefundPolicy refundPolicy;

    @BeforeEach
    void setUp() {
        // --- 공통으로 사용할 실제 도메인 객체들을 생성 ---
        
        // 운영 정책: 매일 9시부터 22시까지, 60분 단위
        List<OperationPolicy.ScheduleDraft> scheduleDrafts = List.of(
                new OperationPolicy.ScheduleDraft(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false),
                new OperationPolicy.ScheduleDraft(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false),
                new OperationPolicy.ScheduleDraft(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false),
                new OperationPolicy.ScheduleDraft(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false),
                new OperationPolicy.ScheduleDraft(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false),
                new OperationPolicy.ScheduleDraft(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false),
                new OperationPolicy.ScheduleDraft(DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(22, 0), false)
        );
        operationPolicy = OperationPolicy.createWith7Days("Standard Policy", SlotUnit.MINUTES_60, scheduleDrafts);

        // 룸 규칙: 최소 60분, 30일 이내 예약 가능
        roomRule = RoomRule.createRoomRule("Standard Rule", 60, 30, true);

        // 환불 정책
        List<RefundRule> refundRules = List.of(RefundRule.createRule("Standard", 0, 100));
        refundPolicy = RefundPolicy.createPolicy("Standard Refund", refundRules);

        // 룸: 시간당 10000원
        room = Room.create(operationPolicy, roomRule, 1L, "Test Room", 4, 10000, Set.of(Room.AmenityType.WIFI));

        // 회원
        member = Member.createUser("testUser", "test@example.com", "password", "010-1234-5678");
    }


    @Test
    @DisplayName("예약 생성 성공 - 실제 도메인 로직 검증")
    void createReservation_Success_WithRealDomainLogic() {
        // given
        Long memberId = 1L;
        Long roomId = 100L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endTime = startTime.plusHours(2);

        ReservationCreateRequest request = new ReservationCreateRequest(roomId, startTime, endTime, true);

        // 실제 Room 객체를 Mocking하여 ID를 제어
        Room mockedRoom = mock(Room.class);
        given(mockedRoom.getId()).willReturn(roomId); // ID 반환 설정
        given(mockedRoom.getOperationPolicy()).willReturn(operationPolicy);
        given(mockedRoom.getRoomRule()).willReturn(roomRule);
        given(mockedRoom.calculatePriceFor(startTime, endTime)).willReturn(20000);

        // 외부 의존성 Mocking
        given(roomQueryService.getById(roomId)).willReturn(mockedRoom);
        given(reservationRepository.existsActiveReservation(roomId, startTime, endTime)).willReturn(false);

        // Mapper가 실제 Reservation 객체를 생성하도록 설정
        Reservation tempReservation = Reservation.createTemp(memberId, roomId, 1L, 1L, startTime, endTime, 20000, startTime.minusMinutes(10));
        given(reservationMapper.toTempReservation(any(), any(), any(), any(Integer.class), any())).willReturn(tempReservation);

        // when
        Long reservationId = reservationService.createReservation(request, memberId);

        // then
        assertThat(reservationId).isEqualTo(tempReservation.getId());
        verify(reservationRepository).save(any(Reservation.class));
        assertThat(tempReservation.getTotalAmount()).isEqualTo(20000);
    }

    @Test
    @DisplayName("예약 생성 실패 - 운영 시간 위반 (Edge Case)")
    void createReservation_Fail_OutOfOperationHours() {
        // given
        Long memberId = 1L;
        Long roomId = 100L;
        // 운영 시간(9시~22시)을 벗어나는 새벽 4시로 예약 시도
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(4).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(1);

        ReservationCreateRequest request = new ReservationCreateRequest(roomId, startTime, endTime, true);

        given(roomQueryService.getById(roomId)).willReturn(room);

        // when & then
        // Room의 OperationPolicy에 정의된 validateWithinOperatingHours가 예외를 발생시키는지 검증
        assertThatThrownBy(() -> reservationService.createReservation(request, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_OUT_OF_OPERATION_HOURS);
    }

    @Test
    @DisplayName("예약 생성 실패 - 최소 예약 시간 미달 (Edge Case)")
    void createReservation_Fail_MinDurationNotMet() {
        // given
        Long memberId = 1L;
        Long roomId = 100L;
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        // 룸 규칙(최소 60분)에 미달하는 30분 예약 시도
        LocalDateTime endTime = startTime.plusMinutes(30);

        ReservationCreateRequest request = new ReservationCreateRequest(roomId, startTime, endTime, true);

        given(roomQueryService.getById(roomId)).willReturn(room);

        // when & then
        // Room의 RoomRule에 정의된 validateReservable이 예외를 발생시키는지 검증
        assertThatThrownBy(() -> reservationService.createReservation(request, memberId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.RES_MIN_DURATION_NOT_MET);
    }

    @Test
    @DisplayName("예약 취소 성공 - TEMP 상태")
    void cancelReservation_Success_TempStatus() {
        // given
        Long reservationId = 123L;
        Long memberId = 1L;
        
        Reservation reservation = Reservation.createTemp(memberId, 100L, 1L, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(1), 10000, LocalDateTime.now().plusMinutes(10));

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));

        // when
        reservationService.cancelReservation(reservationId, memberId);

        // then
        verify(reservationTransactionHelper).cancelTempReservation(reservationId);
        verify(paymentService, never()).cancelPayment(any(), any(Long.class), any());
        verify(reservationTransactionHelper, never()).completeCancellation(any(), any(), any(Long.class));
    }

    @Test
    @DisplayName("예약 취소 성공 - CONFIRMED 상태, 환불금 발생")
    void cancelReservation_Success_Confirmed_WithRefund() {
        // given
        Long reservationId = 123L;
        Long memberId = 1L;
        int totalAmount = 20000;
        LocalDateTime startTime = LocalDateTime.now().plusDays(3);

        Reservation reservation = Reservation.createTemp(memberId, 100L, 1L, 1L, startTime, startTime.plusHours(2), totalAmount, LocalDateTime.now().plusMinutes(10));
        reservation.confirm(LocalDateTime.now());

        given(reservationRepository.findById(reservationId)).willReturn(Optional.of(reservation));
        given(paymentRepository.findByReservationId(reservationId)).willReturn(Optional.of(mock(Payment.class)));
        
        RefundCalculationResponse refundInfo = new RefundCalculationResponse(1L, "Test Policy", Collections.emptyList(), 100, 20000L, totalAmount);
        given(refundPolicyService.calculateRefundAmount(any(), eq(totalAmount), eq(startTime))).willReturn(refundInfo);

        // when
        reservationService.cancelReservation(reservationId, memberId);

        // then
        verify(paymentService).cancelPayment(eq(reservationId), eq(20000L), anyString());
        verify(reservationTransactionHelper).completeCancellation(eq(reservationId), any(), eq(20000L));
    }
}
