// 패키지 경로 수정
package com.example.studyroomreservation.domain.reservation.scheduler;

import com.example.studyroomreservation.domain.reservation.repository.ReservationBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationBatchRepository reservationBatchRepository;

    // 이전 실행 완료 시점 기준 5분 후 실행
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void expireTempReservations() {
        LocalDateTime now = LocalDateTime.now();

        long expiredCount = reservationBatchRepository.bulkExpireTempReservations(now);
        if (expiredCount > 0) {
            log.info("[Scheduler] 만료된 임시 예약 {}건 정리 완료", expiredCount);
        }
    }

    // 30분 마다 실행: 매 시간 정각
    @Scheduled(cron = "0 0,30 * * * *")
    public void completeUsedReservations() {
        LocalDateTime now = LocalDateTime.now();

        long usedCount = reservationBatchRepository.bulkCompleteUsedReservations(now);
        if (usedCount > 0) {
            log.info("[Scheduler] 이용 종료된 예약 {}건 완료 처리", usedCount);
        }
    }
}
