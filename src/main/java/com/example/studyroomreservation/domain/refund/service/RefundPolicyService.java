package com.example.studyroomreservation.domain.refund.service;

import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyDetailResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyListResponse;
import com.example.studyroomreservation.domain.refund.dto.response.RefundPolicyPickItemResponse;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.mapper.RefundMapper;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
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
    public Page<RefundPolicyListResponse> getRefundPolicyPage(Boolean isActive, Pageable pageable) {
        return refundPolicyRepository.findPolicyPageWithRuleCount(isActive, pageable);
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
        //FIXME: 검증 추가 필요, 공통화 하기

        policy.changeActive(active);
    }

    // 룸 등록시 정책 목록 조회용
    public List<RefundPolicyPickItemResponse> getActivePickItems() {
        return refundPolicyRepository.findActivePickItems();
    }

    public Long validateRefundPolicy(Long id) {
        RefundPolicy policy = refundPolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REF_POLICY_NOT_FOUND));

        if (!policy.isActive()) {
            throw new BusinessException(ErrorCode.REF_POLICY_INACTIVE,
                    "비활성화된 환불 정책: id=" + id);
        }
        return policy.getId();
    }

    // 룸 수정시 폼에서 보여줄 적용된 정책 이름 조회용
    public String getRefundPolicyName(Long refundPolicyId) {
        return refundPolicyRepository.findById(refundPolicyId)
                .map(RefundPolicy::getName)
                .orElse("");
    }
}

