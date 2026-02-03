package com.example.studyroomreservation.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MemberUpdateRequest(

        @NotBlank(message = "이름을 입력해주세요.")
        @Size(max=10, message = "이름은 10자 이하여야 합니다.")
        String name,

        @NotBlank
        @Pattern(regexp = "^[0-9]{9,20}$|^[0-9]{2,4}-[0-9]{3,4}-[0-9]{4}$",
                message="전화번호는 숫자만 또는 하이픈(-) 포함 형식으로 입력해주세요.")
        String phoneNumber
) {}
