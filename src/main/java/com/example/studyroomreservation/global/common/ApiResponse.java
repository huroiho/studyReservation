package com.example.studyroomreservation.global.common; // 패키지 경로는 적절히 수정하세요

import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final String code;
    private final String message;
    private final T data;

    // 성공 시 데이터와 함께 반환하는 정적 팩토리 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("200", "OK", data);
    }

    // 데이터 없이 성공 메시지만 보낼 때 (예: 삭제 성공)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>("200", "OK", null);
    }

    private ApiResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}