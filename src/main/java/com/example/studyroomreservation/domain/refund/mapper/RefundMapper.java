package com.example.studyroomreservation.domain.refund.mapper;

import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.dto.request.RefundRuleRequest;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.entity.RefundRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RefundMapper {

    default RefundPolicy createPolicy(RefundPolicyRequest request) {
        if (request == null) {
            return null;
        }
        List<RefundRule> rules = request.rules().stream()
                .map(this::createRule)

                //TODO: 확인하기 - 자바 버전 상 collection.toList() 사용이 다르게 될 수 있음
                .collect(Collectors.toList());
        return RefundPolicy.createPolicy(request.name(), rules);
    }

    default RefundRule createRule(RefundRuleRequest request) {
        if (request == null) {
            return null;
        }

        return RefundRule.createRule(
                request.name(),
                request.refundBaseMinutes(),
                request.refundRate()
        );
    }
}
