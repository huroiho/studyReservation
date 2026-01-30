package com.example.studyroomreservation.domain.room.dto.response;

import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.SlotUnit;

import java.util.List;

public record RoomCreateResponse(

        // 정책 추가 (드롭다운)
        List<OperationPolicyOption> operationPolicies,
        List<RefundPolicyOption> refundPolicies,

        // 선택된 정책 (아래 표 표시용, 없으면 empty)
        List<SelectedOperationPolicy> selectedOperationPolicies,
        List<SelectedRefundPolicy> selectedRefundPolicies,

        // 편의시설
        List<Room.AmenityType> amenities
) {
    public record OperationPolicyOption(
            Long id,
            String name,
            String daySummary,
            String timeSummary
    ) {
        public String label() {
            return name + " (" + daySummary + ", " + timeSummary + ")";
        }
    }

    public record SelectedOperationPolicy(
            Long id,
            String name,
            String daySummary,
            String timeSummary,
            SlotUnit slotUnit,
            String minUsage
    ) {}

    public record RefundPolicyOption(
            Long id,
            String name,
            long ruleCount
    ) {
        public String label() {
            return name + " (룰 " + ruleCount + "개)";
        }
    }

    public record SelectedRefundPolicy(
            Long id,
            String name,
            long ruleCount
    ) {}
}