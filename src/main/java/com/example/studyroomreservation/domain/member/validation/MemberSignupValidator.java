package com.example.studyroomreservation.domain.member.validation;

import com.example.studyroomreservation.domain.member.dto.request.MemberSignupRequest;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class MemberSignupValidator implements Validator  {

    private final MemberRepository memberRepository;

    private static final String PHONE_PATTERN = "^0\\d{1,2}-?\\d{3,4}-?\\d{4}$";

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberSignupRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberSignupRequest request = (MemberSignupRequest) target;

        validateEmailUniqueness(request, errors);
        validatePhoneNumberFormat(request, errors);
        validatePhoneNumberUniqueness(request, errors);
    }

    private void validateEmailUniqueness(MemberSignupRequest request, Errors errors) {
        String email = request.email();
        if (email == null || email.isBlank()) return;

        email = email.trim();

        if (memberRepository.existsByEmail(email)) {
            errors.rejectValue("email", "M002", "이미 사용 중인 이메일입니다.");
        }
    }
    
    private void validatePhoneNumberFormat(MemberSignupRequest request, Errors errors) {
        String phone = request.phoneNumber();
        if (phone == null || phone.isBlank()) return;

        phone = phone.trim();

        if (!phone.matches(PHONE_PATTERN)) {
            errors.rejectValue("phoneNumber", "M007", "전화번호 형식이 올바르지 않습니다.");
        }
    }

    private void validatePhoneNumberUniqueness(MemberSignupRequest request, Errors errors) {
        String phone = request.phoneNumber();
        if (phone == null || phone.isBlank()) return;
        
        phone = phone.trim();
        if (!phone.matches(PHONE_PATTERN)) return;

        phone = phone.replaceAll("-", "");

        if (memberRepository.existsByPhoneNumber(phone)) {
            errors.rejectValue("phoneNumber", "M003", "이미 사용 중인 전화번호입니다.");
        }
    }

}
