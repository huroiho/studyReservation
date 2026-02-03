package com.example.studyroomreservation.domain.member.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberSignupRequest(

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max = 50)
        String name,

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
                message = "비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 최소 1개씩 포함해야 합니다.")
        String password,

        @NotBlank(message = "전화번호를 입력해주세요.")
        @Pattern(
                regexp = "^[0-9]{9,20}$|^[0-9]{2,4}-[0-9]{3,4}-[0-9]{4}$",
                message = "전화번호는 숫자만 또는 하이픈(-) 포함 형식으로 입력해주세요."
        )
        String phoneNumber
) {}
