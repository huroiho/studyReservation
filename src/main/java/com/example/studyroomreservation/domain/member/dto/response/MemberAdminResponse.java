package com.example.studyroomreservation.domain.member.dto.response;

import com.example.studyroomreservation.domain.member.entity.Role;

import java.time.LocalDateTime;

public record MemberAdminResponse(
        String name,
        String email,
        String phoneNumber,
        Role role,
        LocalDateTime createdAt,
        LocalDateTime deletedAt
) {
}
