package com.example.studyroomreservation.domain.refund.service;

import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyDetailResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyListResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundRuleResponse;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.mapper.RefundMapper;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.domain.refund.repository.RefundRuleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundRuleRepository refundRuleRepository;
    private final RefundMapper refundMapper;

    @Transactional
    public Long registerPolicy(RefundPolicyRequest request){

        RefundPolicy newPolicy = refundMapper.toEntity(request);

        RefundPolicy savedPolicy = refundPolicyRepository.save(newPolicy);

        return savedPolicy.getId();
    }

    @Transactional(readOnly = true)
    public Page<RefundPolicyListResponse> getRefundPolicyPage(Pageable pageable) {
        return refundPolicyRepository.findAll(pageable)
                .map(this::toListRes);
    }

    @Transactional(readOnly = true)
    public RefundPolicyDetailResponse getRefundPolicyDetail(Long policyId) {
        RefundPolicy policy = refundPolicyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("환불 정책이 존재하지 않습니다. id=" + policyId));

        List<RefundRuleResponse> rules = policy.getRules().stream()
                .map(r -> new RefundRuleResponse(
                        r.getId(),
                        r.getName(),
                        r.getRefundBaseMinutes(),
                        r.getRefundRate(),
                        r.getCreatedAt()
                ))
                .toList();

        return new RefundPolicyDetailResponse(
                policy.getId(),
                policy.getName(),
                policy.isActive(),
                policy.getCreatedAt(),
                policy.getActiveUpdatedAt(),
                rules
        );
    }

    private RefundPolicyListResponse toListRes(RefundPolicy policy) {
        long ruleCount = refundRuleRepository.countByRefundPolicyId(policy.getId());

        return new RefundPolicyListResponse(
                policy.getId(),
                policy.getName(),
                policy.isActive(),
                ruleCount,
                policy.getCreatedAt(),
                policy.getActiveUpdatedAt()
        );
    }
}

