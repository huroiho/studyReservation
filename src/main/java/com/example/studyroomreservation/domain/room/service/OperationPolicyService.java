package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.mapper.OperationPolicyMapper;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationPolicyService {

    private final OperationPolicyRepository operationPolicyRepository;
    private final OperationPolicyMapper operationPolicyMapper;

    /**
     * 운영 정책 생성
     * - 검증은 OperationPolicyValidator에서 이미 완료됨
     * - 비즈니스 로직만 수행
     */
    @Transactional
    public Long create(OperationPolicyCreateRequest request){
        OperationPolicy newPolicy = operationPolicyMapper.createPolicy(request);
        return operationPolicyRepository.save(newPolicy).getId();
    }

    public OperationPolicyDetailResponse getDetail(Long id) {
        OperationPolicy policy = operationPolicyRepository.findByIdWithSchedules(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.OP_POLICY_NOT_FOUND));
        return operationPolicyMapper.toDetailResponse(policy);
    }
}
