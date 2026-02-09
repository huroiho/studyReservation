package com.example.studyroomreservation.domain.reservation.entity;

import com.example.studyroomreservation.global.common.BaseAuditableEntity;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import static com.example.studyroomreservation.domain.reservation.entity.ReservationStatus.*;

// TODO: where 절의 순서와 인덱스의 columnList 순서 맞추기
@Entity
@Table(
        name = "reservations",
        indexes = {
                @Index(
                        name = "idx_reservation_status_expires",
                        columnList = "status, expires_at"
                ),
                @Index(
                        name = "idx_reservation_room_status_time",
                        columnList = "room_id, status, start_time, end_time"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseAuditableEntity {

    @Column(name="member_id", nullable=false)
    private Long memberId;

    @Column(name="room_id", nullable=false)
    private Long roomId;

    @Column(name = "applied_operation_policy_id", nullable = false)
    private Long appliedOperationPolicyId;

    @Column(name = "applied_refund_policy_id", nullable = false)
    private Long appliedRefundPolicyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;            // KRW, 원 단위

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;    // TEMP 상태 전용: 만료 예정 시각

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;  // 우리 시스템에서 CONFIRMED로 바꾼 시각(= 서비스에서 확정 처리한 시간) != PG “승인 시각”

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;   // 취소 확정 시각

    @Column(name = "used_at")
    private LocalDateTime usedAt;       // 이용 완료 처리 시각


    private Reservation(Long memberId,
                        Long roomId,
                        Long appliedOperationPolicyId,
                        Long appliedRefundPolicyId,
                        ReservationStatus status,
                        LocalDateTime startTime,
                        LocalDateTime endTime,
                        int totalAmount,
                        LocalDateTime expiresAt
                        ) {
        this.memberId = memberId;
        this.roomId = roomId;
        this.appliedOperationPolicyId = appliedOperationPolicyId;
        this.appliedRefundPolicyId = appliedRefundPolicyId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAmount = totalAmount;
        this.expiresAt = expiresAt;
    }

    // ==== 정적 팩토리 메서드 ===
    //TODO: 인자값이 많고 개발 시 값 설정에 오류가 발생할 확률이 높아서 논의 필요
    public static Reservation createTemp(Long memberId,
                                         Long roomId,
                                         Long appliedOperationPolicyId,
                                         Long appliedRefundPolicyId,
                                         LocalDateTime startTime,
                                         LocalDateTime endTime,
                                         int totalAmount,
                                         LocalDateTime expiresAt){
        if (memberId == null || roomId == null)
            throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING, "memberId/roomId");

        if (appliedOperationPolicyId == null || appliedRefundPolicyId == null)
            throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING, "policyId");

        if (startTime == null || endTime == null)
            throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING, "startTime/endTime");

        if(!startTime.isBefore(endTime)) throw new BusinessException(ErrorCode.RES_INVALID_TIME_RANGE);
        if (expiresAt == null) throw new BusinessException(ErrorCode.RES_TEMP_EXPIRES_AT_REQUIRED);
        if (totalAmount < 0) throw new BusinessException(ErrorCode.RES_NEGATIVE_AMOUNT);

        return new Reservation(memberId,roomId,
                appliedOperationPolicyId,
                appliedRefundPolicyId,
                TEMP,
                startTime,
                endTime,
                totalAmount,
                expiresAt);
    }

    // === 상태 변경 ===
    public void expire(LocalDateTime now){
        validateTransitionTime(now);
        validateTransitionTo(EXPIRED);
        if (!isTempExpiredAt(now)) {
            throw new BusinessException(ErrorCode.RES_NOT_EXPIRED_YET);
        }

        this.status = EXPIRED;
        this.expiresAt = null;
    }

    public void confirm(LocalDateTime now){
        validateTransitionTime(now);
        validateTransitionTo(CONFIRMED);
        if (isTempExpiredAt(now)) {
            throw new BusinessException(ErrorCode.RES_ALREADY_EXPIRED);
        }

        status = CONFIRMED;
        this.confirmedAt = now;
        expiresAt = null;
    }

    public void cancel(LocalDateTime now){
        validateTransitionTime(now);
        validateTransitionTo(CANCELED);

        if (!isCancellable(now)) {
            //TODO: 환불 기능 들어가면 isCancellable 로직에 CONFIRM 상태에서의 취소 가능성 검토하기
            throw new BusinessException(ErrorCode.RES_CANCEL_NOT_ALLOWED);
        }

        this.status = CANCELED;
        this.canceledAt = now;
        this.expiresAt = null;
    }

    // TODO : USING 상태 추가 후 변경 필요(reservation 논의사항 참고)
    public void completeUsage(LocalDateTime now){
        validateTransitionTime(now);
        validateTransitionTo(USED);

        if(now.isBefore(endTime)){
            throw new BusinessException(ErrorCode.RES_NOT_ENDED_YET);
        }

        this.status = USED;
        this.usedAt = now;
        this.expiresAt = null;
    }

    public boolean isTempExpiredAt(LocalDateTime now){
        return status == TEMP && expiresAt != null && !now.isBefore(expiresAt);
    }

    public void extendExpiresAt(int minutes){
        if(this.expiresAt == null){
            throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING);
        }

        this.expiresAt = this.expiresAt.plusMinutes(minutes);

    }

    public boolean isCancellable(LocalDateTime now) {
        // 결제 대기(TEMP) 상태일 때 -> 만료 시간이 존재하고, 현재 시간이 만료 시간 전이어야 취소 가능
        if (this.status == TEMP) {
            return this.expiresAt != null && now.isBefore(this.expiresAt);
        }

        //TODO: 예약 확정(CONFIRMED) 상태일 때의 취소 로직 추가하기
        return false;
    }

    // ===== 검증 메서드 =====
    private void validateTransitionTo(ReservationStatus target) {
        if (!this.status.canChangeTo(target)) {
            throw new BusinessException(ErrorCode.RES_STATE_TRANSITION_NOT_ALLOWED,
                    this.status + " -> "+target);
        }
    }

    private static void validateTransitionTime(LocalDateTime now) {
        if (now == null) throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING, "now");
    }

    // 결제시 검증용 메서드
    public void validatePayableForApprove(LocalDateTime now) {
        if (now == null) {
            throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING, "now");
        }

        if (this.status != TEMP) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_STATUS,"결제 승인 가능한 예약 상태가 아닙니다.");
        }

        if (this.expiresAt == null || !now.isBefore(this.expiresAt)) {
            throw new BusinessException(ErrorCode.RES_ALREADY_EXPIRED,"예약 유효 시간이 만료되었습니다.");
        }
    }

    public boolean isPayable(LocalDateTime now) {
        if (now == null) throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING, "now");
        return this.status == TEMP
                && this.expiresAt != null
                && now.isBefore(this.expiresAt);
    }

}
