package com.example.studyroomreservation.domain.member.service;

import com.example.studyroomreservation.domain.member.dto.request.MemberPasswordChangeRequest;
import com.example.studyroomreservation.domain.member.dto.request.MemberSignupRequest;
import com.example.studyroomreservation.domain.member.dto.request.MemberUpdateRequest;
import com.example.studyroomreservation.domain.member.dto.response.MemberAdminResponse;
import com.example.studyroomreservation.domain.member.dto.response.MemberInfoResponse;
import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.mapper.MemberMapper;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberMapper memberMapper;

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

    public MemberInfoResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return memberMapper.toMyInfoResponse(member);
    }

    @Transactional
    public void updateMyProfile(Long memberId, MemberUpdateRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

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
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String encoded = passwordEncoder.encode(request.newPassword());
        member.changePassword(encoded);
    }

    public Page<MemberAdminResponse> getMembersForAdmin(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            // 이름 또는 이메일로 검색 (Repository에 정의 필요)
            return memberRepository.findByNameContainingOrEmailContaining(keyword, keyword, pageable)
                    .map(memberMapper::toMemberAdminResponse); // 엔티티를 DTO로 변환
        }
        return memberRepository.findAll(pageable)
                .map(memberMapper::toMemberAdminResponse);
    }
}
