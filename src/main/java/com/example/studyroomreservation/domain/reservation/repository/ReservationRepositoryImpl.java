package com.example.studyroomreservation.domain.reservation.repository;


import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservableTimeResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.studyroomreservation.domain.reservation.entity.QReservation.reservation;

@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom{

    private final JPAQueryFactory queryFactory;

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

    // 현재 예약하지 못하는 시간대 조회 -> TODO: 군이 테이블 한 열 조회가 아닌 시간대 조회만으로도 충분하지 않을까? 검토하기 프로젝션으로 하기?
    @Override
    public List<RoomReservableTimeResponse> findActiveReservations(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        return queryFactory
                .select(Projections.constructor(RoomReservableTimeResponse.class,
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

    // notin, in은 조회 성능 효율이 안좋음
    private BooleanExpression activeReservationStatus(){
        return reservation.status.notIn(ReservationStatus.EXPIRED, ReservationStatus.CANCELED);
    }

    // 마이페이지 예약현황 조회
    @Override
    public List<Reservation> findMyActiveReservations(Long memberId, LocalDateTime now) {
        return queryFactory
                .selectFrom(reservation)
                .where(
                        reservation.memberId.eq(memberId), // 내 예약
                        reservation.status.in(ReservationStatus.CONFIRMED, ReservationStatus.TEMP),
                        isActiveStatus(now)
                )
                .orderBy(reservation.startTime.asc())
                .fetch();
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
