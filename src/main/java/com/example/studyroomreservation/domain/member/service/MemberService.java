package com.example.studyroomreservation.domain.member.service;

import com.example.studyroomreservation.domain.member.dto.request.MemberSignupRequest;
import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void signup(MemberSignupRequest request) {

        String encodedPassword = passwordEncoder.encode(request.password());
        String normalizedPhoneNumber = request.phoneNumber().replaceAll("-", "");

        Member member = Member.createUser(
                request.name(),
                request.email(),
                encodedPassword,
                normalizedPhoneNumber
        );

        memberRepository.save(member);
    }
}
