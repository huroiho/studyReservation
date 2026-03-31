package com.example.studyroomreservation.domain.member.validation;

import com.example.studyroomreservation.domain.member.dto.request.MemberPasswordChangeRequest;
import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class MemberPasswordChangeValidator implements Validator {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberPasswordChangeRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberPasswordChangeRequest req = (MemberPasswordChangeRequest) target;

        if (req.newPassword() != null && req.newPasswordConfirm() != null
                && !req.newPassword().equals(req.newPasswordConfirm())) {
            errors.rejectValue("newPasswordConfirm", "M005", "새 비밀번호 확인이 일치하지 않습니다.");
        }

        Long memberId = getCurrentMemberId();
        if (memberId == null) return;

        String current = req.currentPassword();
        if (current == null || current.isBlank()) return;

        Member member = memberRepository.findById(memberId).orElse(null);
        if (member == null) return;

        if (!passwordEncoder.matches(current, member.getPassword())) {
            errors.rejectValue("currentPassword", "M006", "현재 비밀번호가 올바르지 않습니다.");
        }
    }

    private Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getMember().getId();
        }
        return null;
    }
}
