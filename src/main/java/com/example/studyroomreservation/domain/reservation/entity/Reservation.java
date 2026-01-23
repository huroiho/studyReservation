package com.example.studyroomreservation.domain.reservation.entity;

import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.global.common.BaseAuditableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applied_operation_policy_id", nullable = false)
    private OperationPolicy appliedOperationPolicy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applied_refund_policy_id", nullable = false)
    private RefundPolicy appliedRefundPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;    // TEMP 상태일 때의 만료 시간

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;  // 결제 완료 시점

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;   // 취소 시점


    private Reservation(Member member,
                        Room room,
                        OperationPolicy appliedOperationPolicy,
                        RefundPolicy appliedRefundPolicy,
                        ReservationStatus status,
                        LocalDateTime startTime,
                        LocalDateTime endTime,
                        int totalAmount,
                        LocalDateTime expiresAt,
                        LocalDateTime confirmedAt,
                        LocalDateTime canceledAt
                        ) {
        this.member = member;
        this.room = room;
        this.appliedOperationPolicy = appliedOperationPolicy;
        this.appliedRefundPolicy = appliedRefundPolicy;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalAmount = totalAmount;
        this.expiresAt = expiresAt;
        this.confirmedAt = confirmedAt;
        this.canceledAt = canceledAt;
    }

    // ==== 정적 팩토리 메서드 ===
    public static Reservation createTemp(Member member,
                                         Room room,
                                         OperationPolicy appliedOperationPolicy,
                                         RefundPolicy appliedRefundPolicy,
                                         ReservationStatus status,
                                         LocalDateTime startTime,
                                         LocalDateTime endTime,
                                         int totalAmount){

        return new Reservation(member, room, appliedOperationPolicy, appliedRefundPolicy, status, startTime, endTime, totalAmount,
                                LocalDateTime.now().plusMinutes(10), null, null
                                );
    }

    // === 상태 변경 ===
    // 이전 상태 확인해서 상태변경 가능한지 체크
    public void confirm(LocalDateTime confirmedAt){
        this.status = ReservationStatus.CONFIRMED;
        this.confirmedAt = confirmedAt;
    }

    public void cancel(LocalDateTime canceledAt){
        this.status = ReservationStatus.CANCELED;
        this.canceledAt = canceledAt;
    }

    public void markUsed(LocalDateTime usedAt){
        this.status = ReservationStatus.USED;
    }

    public void markCanceled(LocalDateTime canceledAt){

    }
}
