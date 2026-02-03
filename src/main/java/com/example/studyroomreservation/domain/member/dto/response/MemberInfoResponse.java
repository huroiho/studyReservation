package com.example.studyroomreservation.domain.member.dto.response;

public record MemberInfoResponse(
        String name,
        String email,
        String phoneNumber
) {
}
