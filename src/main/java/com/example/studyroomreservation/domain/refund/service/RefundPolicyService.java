package com.example.studyroomreservation.domain.refund.service;

import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.mapper.RefundMapper;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundMapper refundMapper;

    @Transactional
    public Long registerPolicy(RefundPolicyRequest request){

        if (refundPolicyRepository.existsByName(request.name())) {
            log.warn("정책 등록 실패 - 중복된 정책명: policyName={}", request.name());
            throw new BusinessException(
                    ErrorCode.REF_POLICY_NAME_DUPLICATE,
                    "policy name: " + request.name()
            );
        }

        RefundPolicy newPolicy = refundMapper.toEntity(request);
        RefundPolicy savedPolicy = refundPolicyRepository.saveAndFlush(newPolicy);

        return savedPolicy.getId();
    }
}
