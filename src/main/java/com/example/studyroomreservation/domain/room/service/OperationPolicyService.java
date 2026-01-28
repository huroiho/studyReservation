package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.mapper.OperationPolicyMapper;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationPolicyService {

    private final OperationPolicyRepository operationPolicyRepository;
    private final OperationPolicyMapper operationPolicyMapper;

    @Transactional
    public Long create(OperationPolicyCreateRequest request){

        if(operationPolicyRepository.existsByName(request.name()))
            throw new BusinessException(ErrorCode.OP_POLICY_NAME_DUPLICATE);

        OperationPolicy newPolicy = operationPolicyMapper.createPolicy(request);

        return operationPolicyRepository.save(newPolicy).getId();
    }

}
