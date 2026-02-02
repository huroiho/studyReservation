package com.example.studyroomreservation.domain.reservation.repository;


import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservedTimeResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.studyroomreservation.domain.reservation.entity.QReservation.reservation;
import static com.example.studyroomreservation.domain.reservation.entity.ReservationStatus.*;

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
}
