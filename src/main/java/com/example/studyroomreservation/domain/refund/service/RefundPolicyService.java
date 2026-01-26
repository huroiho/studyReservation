package com.example.studyroomreservation.domain.refund.service;

import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyDetailResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyListResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundRuleResponse;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.mapper.RefundMapper;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.domain.refund.repository.RefundRuleRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
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
                .map(refundPolicy -> {
                    long ruleCount = refundRuleRepository.countByRefundPolicyId(refundPolicy.getId());
                    return refundMapper.toListResponse(refundPolicy, ruleCount);
                });
    }

    @Transactional(readOnly = true)
    public RefundPolicyDetailResponse getRefundPolicyDetail(Long policyId) {
        RefundPolicy refundPolicy = refundPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REF_POLICY_NOT_FOUND));

        return refundMapper.toDetailResponse(refundPolicy);
    }
}

