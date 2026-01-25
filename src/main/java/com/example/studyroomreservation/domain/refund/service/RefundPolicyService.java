package com.example.studyroomreservation.domain.refund.service;

import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.mapper.RefundMapper;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundMapper refundMapper;

    @Transactional
    public Long registerPolicy(RefundPolicyRequest request){

        RefundPolicy newPolicy = refundMapper.toEntity(request);

        RefundPolicy savedPolicy = refundPolicyRepository.save(newPolicy);

        return savedPolicy.getId();
    }

}
