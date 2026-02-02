package com.example.studyroomreservation.global.config;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile({"local", "dev"})
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // 관리자 계정
        if (memberRepository.findByEmail("admin@test.com").isEmpty()) {
            Member admin = Member.createAdmin(
                    "관리자",
                    "admin@test.com",
                    passwordEncoder.encode("1234"),
                    "010-0000-0000"
            );
            memberRepository.save(admin);
        }

        // 일반 사용자 계정
        if (memberRepository.findByEmail("user@test.com").isEmpty()) {
            Member user = Member.createUser(
                    "일반유저",
                    "user@test.com",
                    passwordEncoder.encode("1234"),
                    "010-1111-1111"
            );
            memberRepository.save(user);
        }
    }
}