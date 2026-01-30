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
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundRuleRepository refundRuleRepository;
    private final RefundMapper refundMapper;

    /**
     * 환불 정책 등록
     * - 정책명 중복 검증은 RefundPolicyValidator에서 처리됨
     */
    @Transactional
    public Long registerPolicy(RefundPolicyRequest request){
        RefundPolicy newPolicy = refundMapper.createPolicy(request);
        RefundPolicy savedPolicy = refundPolicyRepository.save(newPolicy);
        return savedPolicy.getId();
    }

    @Transactional(readOnly = true)
    public Page<RefundPolicyListResponse> getRefundPolicyPage(Boolean isActive,Pageable pageable) {

        Page<RefundPolicy> page = (isActive == null)
                ? refundPolicyRepository.findAll(pageable)
                : refundPolicyRepository.findByIsActive(isActive, pageable);
        return page.map(refundPolicy -> {
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

    @Transactional
    public void changePolicyActive(Long policyId, boolean active) {
        RefundPolicy policy = refundPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REF_POLICY_NOT_FOUND));

        policy.changeActive(active);
    }
}

