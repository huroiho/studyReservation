package com.example.studyroomreservation.domain.reservation.repository;

import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.example.studyroomreservation.domain.reservation.entity.QReservation.reservation;

@Repository
@RequiredArgsConstructor
public class ReservationBatchRepository {

    private final JPAQueryFactory queryFactory;
    // 영속성 컨텍스트 초기화를 위해 필요
    private final EntityManager em;

    @Transactional
    public long bulkExpireTempReservations(LocalDateTime now) {
        em.flush();

        long count = queryFactory
                .update(reservation)
                .set(reservation.status, ReservationStatus.EXPIRED)
                .set(reservation.expiresAt, (LocalDateTime) null)
                .where(reservation.status.eq(ReservationStatus.TEMP)
                        .and(reservation.expiresAt.lt(now.plusMinutes(1))))
                .execute();

        em.clear();

        return count;
    }

    //[스케줄러용] 예약 이용 완료(USED) 처리
    @Transactional
    public long bulkCompleteUsedReservations(LocalDateTime now) {
        em.flush();

        long count = queryFactory
                .update(reservation)
                .set(reservation.status, ReservationStatus.USED)
                .set(reservation.expiresAt, (LocalDateTime) null)
                .set(reservation.startTime, now) // 이용 완료 시각 기록
                .where(reservation.status.eq(ReservationStatus.CONFIRMED)
                        .and(reservation.endTime.lt(now)))
                .execute();

        em.clear();

        return count;
    }
}