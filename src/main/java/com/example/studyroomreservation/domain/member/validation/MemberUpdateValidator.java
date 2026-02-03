package com.example.studyroomreservation.domain.member.validation;

import com.example.studyroomreservation.domain.member.dto.request.MemberUpdateRequest;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class MemberUpdateValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberUpdateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberUpdateRequest request = (MemberUpdateRequest) target;

        Long memberId = getCurrentMemberId();
        if (memberId == null) return;


        validatePhoneNumberUniqueness(request, memberId, errors);
    }

    private void validatePhoneNumberUniqueness(MemberUpdateRequest request, Long memberId, Errors errors
    ) {
        String phone = request.phoneNumber();
        if (phone == null || phone.isBlank()) return;

        String normalized = phone.trim().replaceAll("-", "");

        if (memberRepository.existsByPhoneNumberAndIdNot(normalized, memberId)) {
            errors.rejectValue("phoneNumber", "M003", "이미 사용 중인 전화번호입니다.");
        }
    }

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return null;
        }
        return userDetails.getMember().getId();
    }
}
