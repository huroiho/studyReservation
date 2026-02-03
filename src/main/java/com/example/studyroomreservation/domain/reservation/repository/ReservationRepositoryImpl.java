package com.example.studyroomreservation.domain.reservation.repository;


import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservedTimeResponse;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.studyroomreservation.domain.reservation.entity.QReservation.reservation;
import static com.example.studyroomreservation.domain.reservation.entity.ReservationStatus.*;
import static com.example.studyroomreservation.domain.room.entity.QRoom.room;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;


    @Override
    public boolean existsActiveReservation(Long roomId, LocalDateTime start, LocalDateTime end){
        Integer fetchOne = queryFactory
                .selectOne()
                .from(reservation)
                .where(
                        reservation.roomId.eq(roomId),
                        overLappingTime(start, end),
                        activeReservationStatus()
                )
                .fetchFirst();
        return fetchOne != null;
    }

    @Override
    public List<RoomReservedTimeResponse> findActiveReservations(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return queryFactory
                .select(Projections.constructor(RoomReservedTimeResponse.class,
                        reservation.startTime,
                        reservation.endTime))
                .from(reservation)
                .where(
                        reservation.roomId.eq(roomId),
                        overLappingTime(startTime, endTime),
                        activeReservationStatus()
                )
                .orderBy(reservation.startTime.asc())
                .fetch();
    }

    // 시간 겹치는지 확인하는 조건 객체(lt: less than, gt: greater than)
    private BooleanExpression overLappingTime(LocalDateTime start, LocalDateTime end){
        return reservation.startTime.lt(end)
                .and(reservation.endTime.gt(start));
    }

    private BooleanExpression activeReservationStatus() {
        LocalDateTime now = LocalDateTime.now();

        // CONFIRMED 또는 USED. 확정되었거나 진행 중인 예약 (시간 점유 중)
        BooleanExpression confirmedOrUsed = reservation.status.in(CONFIRMED, USED);

        // TEMP이면서 아직 만료되지 않은 경우 (expiresAt > now)
        BooleanExpression tempNotExpired = reservation.status.eq(TEMP)
                .and(reservation.expiresAt.gt(now));

        return confirmedOrUsed.or(tempNotExpired);
    }

    // 마이페이지 예약현황 조회
    // Tuple : 두 종류 이상의 객체 묶을때 사용 (reservation, room), map과 유사한 동작
    @Override
    public List<Tuple> findMyActiveReservationsWithRoom(Long memberId, LocalDateTime now) {
        return queryFactory
                .select(reservation, room)
                .from(reservation)
                .join(room).on(reservation.roomId.eq(room.id))
                //.leftJoin(room.images).fetchJoin()
                .where(
                        reservation.memberId.eq(memberId), // 내 예약
//                        reservation.status.in(ReservationStatus.CONFIRMED, ReservationStatus.TEMP),
                        isActiveStatus(now)
                )
                .orderBy(reservation.startTime.asc())
                .distinct()
                .fetch();
    }

    /**
     * 예약 상태를 원자적(Atomic)으로 검증하고 확정(CONFIRMED) 상태로 변경합니다.
     *
     * @param reservationId 예약 ID
     * @param now 확정 처리 시각
     * @return 업데이트에 성공한 행의 수 (0이면 상태 불일치로 인한 실패)
     */
    @Override
    public long confirmIfTemp(Long reservationId, LocalDateTime now) {
        em.flush();
        long count = queryFactory
                .update(reservation)
                .set(reservation.status, CONFIRMED)
                .set(reservation.confirmedAt, now)
                .set(reservation.expiresAt, (LocalDateTime) null)
                .where(
                        reservation.id.eq(reservationId),
                        reservation.status.eq(TEMP),
                        reservation.expiresAt.gt(now)
                )
                .execute();
        em.clear();

        return count;
    }

    private BooleanExpression isActiveStatus(LocalDateTime now) {
        // 확정 + 종료 전
        BooleanExpression isConfirmed = reservation.status.eq(ReservationStatus.CONFIRMED)
                .and(reservation.endTime.gt(now));

        // 대기 + 결제 시한 + 시작 전
        BooleanExpression isTemp = reservation.status.eq(ReservationStatus.TEMP)
                .and(reservation.expiresAt.gt(now))
                .and(reservation.startTime.gt(now));

        return isConfirmed.or(isTemp);
    }
}
