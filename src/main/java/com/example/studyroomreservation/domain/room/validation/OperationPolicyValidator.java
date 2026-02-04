package com.example.studyroomreservation.domain.room.validation;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest.ScheduleRequest;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.Duration;

/**
 * 운영 정책 생성 시 비즈니스 검증을 수행하는 Validator
 * 검증 범위 :정책명 중복 (DB 조회), 슬롯 정렬 (운영 시간이 슬롯 단위로 나누어 떨어지는지)
 * 검증하지 않음 : 요일 중복 (UI가 고정 7행으로 렌더링)
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

        validateNameUniqueness(request, errors);
        validateSlotAlignment(request, errors);
    }

    private void validateNameUniqueness(OperationPolicyCreateRequest request, Errors errors) {
        if (request.name() != null && operationPolicyRepository.existsByName(request.name())) {
            errors.rejectValue("name", ErrorCode.OP_POLICY_NAME_DUPLICATE.getCode(), ErrorCode.OP_POLICY_NAME_DUPLICATE.getMessage());
        }
    }

    private void validateSlotAlignment(OperationPolicyCreateRequest request, Errors errors) {
        if (request.slotUnit() == null || request.schedules() == null) {
            return;
        }

        int unitMinutes = request.slotUnit().getMinutes();

        for (int i = 0; i < request.schedules().size(); i++) {
            ScheduleRequest schedule = request.schedules().get(i);

            // 휴무일이거나 시간 정보가 불완전하면 스킵 (다른 검증에서 처리)
            if (schedule.closed() ||
                schedule.openTime() == null ||
                schedule.closeTime() == null ||
                !schedule.openTime().isBefore(schedule.closeTime())) {
                continue;
            }

            long durationMinutes = Duration.between(
                    schedule.openTime(),
                    schedule.closeTime()
            ).toMinutes();

            if (durationMinutes % unitMinutes != 0) {
                errors.rejectValue(
                        "schedules[" + i + "]",
                        ErrorCode.OS_NOT_ALIGNED_TO_SLOT.getCode(),
                        ErrorCode.OS_NOT_ALIGNED_TO_SLOT.getMessage()
                );
            }
        }
    }
}
