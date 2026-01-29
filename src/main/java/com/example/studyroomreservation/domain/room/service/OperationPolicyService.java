package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyListResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.mapper.OperationPolicyMapper;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
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

    public Page<OperationPolicyListResponse> getList(Pageable pageable){
        return operationPolicyRepository.findList(pageable);
    }

}
