package com.example.studyroomreservation.domain.reservation.repository;

import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    //FIXME: 락 오류 해결하기
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.id = :id")
    Optional<Reservation> findByIdWithLock(@Param("id") Long id);

    // 운영 정책이 적용된 예약 존재 여부 확인
    boolean existsByAppliedOperationPolicyId(Long appliedOperationPolicyId);
}
