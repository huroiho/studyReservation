package com.example.studyroomreservation.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // COMMON
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "알 수 없는 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다."),

    // REFUND
    REF_POLICY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "RE001", "정책 이름은 필수입니다."),
    REF_RULE_REQUIRED(HttpStatus.BAD_REQUEST, "RE002", "최소 하나의 환불 규칙이 필요합니다."),
    REF_RATE_INVALID(HttpStatus.BAD_REQUEST, "RE003", "환불 비율은 0~100 사이여야 합니다."),
    REF_RULE_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "RE004", "규칙 이름은 필수입니다."),
    REF_POLICY_ALREADY_ASSIGNED(HttpStatus.BAD_REQUEST, "RE005", "이미 환불 정책이 할당되어 있습니다."),
    REF_PAYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "RE006", "환불 대상 결제 정보는 필수입니다."),
    REF_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "RE007", "환불 금액은 0원보다 커야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}