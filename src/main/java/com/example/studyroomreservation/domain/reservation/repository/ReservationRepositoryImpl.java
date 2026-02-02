package com.example.studyroomreservation.domain.reservation.repository;


import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservableTimeResponse;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
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
}
