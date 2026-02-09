package com.example.studyroomreservation.domain.member.service;

import com.example.studyroomreservation.domain.member.dto.request.MemberPasswordChangeRequest;
import com.example.studyroomreservation.domain.member.dto.request.MemberSignupRequest;
import com.example.studyroomreservation.domain.member.dto.request.MemberUpdateRequest;
import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberQueryService memberQueryService;

    public void signup(MemberSignupRequest request) {

        String encodedPassword = passwordEncoder.encode(request.password());
        String normalizedPhoneNumber = request.phoneNumber().replaceAll("-", "");

        Member member = Member.createUser(
                request.name(),
                request.email(),
                encodedPassword,
                normalizedPhoneNumber
        );

        try {
            memberRepository.saveAndFlush(member);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_DUPLICATED);
        }
    }

    public void updateMyProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberQueryService.getById(memberId);

        if (request.name() != null) {
            member.changeName(request.name().trim());
        }
        if (request.phoneNumber() != null) {
            String normalizedPhone =
                    request.phoneNumber().trim().replaceAll("-", "");
            member.changePhoneNumber(normalizedPhone);
        }
    }

    @Transactional
    public void changeMyPassword(Long memberId, MemberPasswordChangeRequest request) {
        Member member = memberQueryService.getById(memberId);

        String encoded = passwordEncoder.encode(request.newPassword());
        member.changePassword(encoded);
    }
}
