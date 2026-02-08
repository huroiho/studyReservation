package com.example.studyroomreservation.domain.refund.service;

import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyDetailResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyListResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyPickItemResponse;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.mapper.RefundMapper;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefundPolicyQueryService {
    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundMapper refundMapper;

    public Long validateRefundPolicy(Long id) {
        RefundPolicy policy = refundPolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REF_POLICY_NOT_FOUND));

        if (!policy.isActive()) {
            throw new BusinessException(ErrorCode.REF_POLICY_INACTIVE,
                    "비활성화된 환불 정책: id=" + id);
        }
        return policy.getId();
    }

    // 룸 등록시 정책 목록 조회용
    public List<RefundPolicyPickItemResponse> getActivePickItems() {
        return refundPolicyRepository.findActivePickItems();
    }

    // 룸 수정시 폼에서 보여줄 적용된 정책 이름 조회용
    public String getRefundPolicyName(Long refundPolicyId) {
        return refundPolicyRepository.findById(refundPolicyId)
                .map(RefundPolicy::getName)
                .orElse("");
    }

    public Page<RefundPolicyListResponse> getRefundPolicyPage(Boolean isActive, Pageable pageable) {
        return refundPolicyRepository.findPolicyPageWithRuleCount(isActive, pageable);
    }

    public RefundPolicyDetailResponse getRefundPolicyDetail(Long policyId) {
        RefundPolicy refundPolicy = refundPolicyRepository.findById(policyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REF_POLICY_NOT_FOUND));

        return refundMapper.toDetailResponse(refundPolicy);
    }
}
