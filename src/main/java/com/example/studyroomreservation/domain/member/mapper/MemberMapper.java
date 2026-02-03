package com.example.studyroomreservation.domain.member.mapper;

import com.example.studyroomreservation.domain.member.dto.response.MemberInfoResponse;
import com.example.studyroomreservation.domain.member.entity.Member;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    default MemberInfoResponse toMyInfoResponse(Member member) {
        if (member == null) return null;

        return new MemberInfoResponse(
                member.getName(),
                member.getEmail(),
                member.getPhoneNumber()
        );
    }
}
