package com.example.studyroomreservation.domain.room.validation.validator;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 운영 정책 생성 시 비즈니스 검증을 수행하는 Validator
 * - DB 조회가 필요한 검증 (정책명 중복 등)을 처리
 * - @Valid와 함께 동작하여 BindingResult에 에러를 자연스럽게 추가
 */
@Component
@RequiredArgsConstructor
public class OperationPolicyValidator implements Validator {

    private final OperationPolicyRepository operationPolicyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return OperationPolicyCreateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OperationPolicyCreateRequest request = (OperationPolicyCreateRequest) target;

        // 정책명 중복 검증
        if (request.name() != null && operationPolicyRepository.existsByName(request.name())) {
            errors.rejectValue("name", "OP007", "이미 존재하는 정책 이름입니다.");
        }

        // 추가 비즈니스 검증이 필요한 경우 여기에 작성
        // 예: 특정 조건에서만 허용되는 슬롯 단위 검증 등
    }
}
