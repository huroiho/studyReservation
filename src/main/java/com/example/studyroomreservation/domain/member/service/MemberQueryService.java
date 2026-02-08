package com.example.studyroomreservation.domain.member.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;

    public MemberInfoResponse getMyInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return memberMapper.toMyInfoResponse(member);
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

    public Member getById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

}
