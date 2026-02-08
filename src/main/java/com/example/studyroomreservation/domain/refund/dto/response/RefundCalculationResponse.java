package com.example.studyroomreservation.domain.refund.dto.response;

import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.entity.RefundRule;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record RefundCalculationResponse(
        Long id,
        String name,
        List<RefundRuleResponse> rules,
        int refundRate,
        long refundAmount,
        long totalAmount
) {}
