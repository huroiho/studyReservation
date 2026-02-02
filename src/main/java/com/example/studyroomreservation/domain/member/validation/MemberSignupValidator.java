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

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberSignupRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberSignupRequest request = (MemberSignupRequest) target;

        validateEmailUniqueness(request, errors);
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

    private void validatePhoneNumberUniqueness(MemberSignupRequest request, Errors errors) {
        String phone = request.phoneNumber();
        if (phone == null || phone.isBlank()) return;

        phone = phone.replaceAll("-", "");

        if (memberRepository.existsByPhoneNumber(phone)) {
            errors.rejectValue("phoneNumber", "M003", "이미 사용 중인 전화번호입니다.");
        }
    }

}
