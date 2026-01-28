package com.example.studyroomreservation.domain.room.validation.validator;

import com.example.studyroomreservation.domain.room.dto.request.RoomRuleCreateRequest;
import com.example.studyroomreservation.domain.room.repository.RoomRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 방 예약 규칙 생성 시 비즈니스 검증을 수행하는 Validator
 * <p>
 * 검증 범위:
 * - 규칙명 중복 (DB 조회)
 */
@Component
@RequiredArgsConstructor
public class RoomRuleValidator implements Validator {

    private final RoomRuleRepository roomRuleRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RoomRuleCreateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RoomRuleCreateRequest request = (RoomRuleCreateRequest) target;

        validateNameUniqueness(request, errors);
    }

    private void validateNameUniqueness(RoomRuleCreateRequest request, Errors errors) {
        if (request.name() != null && roomRuleRepository.existsByName(request.name())) {
            errors.rejectValue("name", "RR001", "이미 존재하는 규칙 이름입니다.");
        }
    }
}
