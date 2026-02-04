package com.example.studyroomreservation.domain.refund.validation;

import com.example.studyroomreservation.domain.refund.dto.request.RefundPolicyRequest;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 환불 정책 생성 시 비즈니스 검증을 수행하는 Validator
 * 검증 범위: 정책명 중복 (DB 조회)
 */
@Component
@RequiredArgsConstructor
public class RefundPolicyValidator implements Validator {

    private final RefundPolicyRepository refundPolicyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RefundPolicyRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RefundPolicyRequest request = (RefundPolicyRequest) target;

        validateNameUniqueness(request, errors);
    }

    private void validateNameUniqueness(RefundPolicyRequest request, Errors errors) {
        if (request.name() != null && refundPolicyRepository.existsByName(request.name())) {
            errors.rejectValue("name", ErrorCode.REF_POLICY_NAME_DUPLICATE.getCode(), ErrorCode.REF_POLICY_NAME_DUPLICATE.getMessage());
        }
    }
}
