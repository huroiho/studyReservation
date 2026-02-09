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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import com.example.studyroomreservation.domain.reservation.dto.response
        .AdminReservationResponse;
import com.example.studyroomreservation.domain.member.entity.QMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.studyroomreservation.domain.reservation.entity.QReservation.reservation;
import static com.example.studyroomreservation.domain.reservation.entity.ReservationStatus.*;
import static com.example.studyroomreservation.domain.room.entity.QRoom.room;
import static com.example.studyroomreservation.domain.member.entity.QMember.member;

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

    @Override
    public Page<AdminReservationResponse> findAllReservationsForAdmin(Pageable pageable) {

        List<AdminReservationResponse> content = queryFactory
                .select(Projections.constructor(AdminReservationResponse.class,
                        reservation.id,
                        member.email,
                        room.name,
                        reservation.status,
                        reservation.startTime,
                        reservation.endTime,
                        reservation.totalAmount,
                        reservation.createdAt
                ))
                .from(reservation)

                // ID 기반 조인
                .leftJoin(member).on(reservation.memberId.eq(member.id))
                .leftJoin(room).on(reservation.roomId.eq(room.id))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())

                // 최신 순으로 정렬 고정(검색이나 다른 정렬 필요 시 수정)
                .orderBy(reservation.id.desc())
                .fetch();

        // 전체 페이지 파악용 쿼리
        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    // 마이페이지 예약 히스토리
    @Override
    public Page<Tuple> findMyReservationHistory(Long memberId, LocalDateTime now, Pageable pageable) {
        List<Tuple> content = queryFactory
                .select(reservation, room)
                .from(reservation)
                .join(room).on(reservation.roomId.eq(room.id))
                .where(
                        reservation.memberId.eq(memberId)
                )
                .orderBy(reservation.startTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 페이징
        Long total = queryFactory
                .select(reservation.count())
                .from(reservation)
                .where(
                        reservation.memberId.eq(memberId)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
