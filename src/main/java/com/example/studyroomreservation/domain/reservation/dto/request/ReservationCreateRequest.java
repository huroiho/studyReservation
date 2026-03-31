package com.example.studyroomreservation.domain.reservation.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationCreateRequest(
        @NotNull
        Long roomId,
        @NotNull
        @Future // 미래 시간만 예약 가능하게 유효성 검사 어노테이션 추가
        LocalDateTime startTime,
        @NotNull
        @Future
        LocalDateTime endTime,
        @NotNull(message = "환불 정책 동의는 필수입니다.")
        @AssertTrue(message = "환불 정책에 동의해야 예약할 수 있습니다.")
        Boolean isRefundPolicyAgreed
) {
        @AssertTrue(message = "종료 시간은 시작 시간보다 뒤여야 합니다.")
        public boolean isEndTimeAfterStartTime() {
                if (startTime == null || endTime == null) return true;
                return endTime.isAfter(startTime);
        }
}
