package com.example.studyroomreservation.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberPasswordChangeRequest(

        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min=8, message="비밀번호는 8자 이상이어야 합니다")
        @Pattern(regexp="^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message="비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 최소 1개씩 포함해야 합니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호를 다시한번 입력해주세요.")
        String newPasswordConfirm
) {}
