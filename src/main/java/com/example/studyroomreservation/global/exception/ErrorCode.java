package com.example.studyroomreservation.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // COMMON
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "알 수 없는 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다."),

    // RESERVATION
    RES_REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "R001", "필수 값이 누락되었습니다."),
    RES_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "R002", "예약 시간이 올바르지 않습니다."),
    RES_NEGATIVE_AMOUNT(HttpStatus.BAD_REQUEST, "R003", "결제 금액이 올바르지 않습니다."),
    RES_TEMP_EXPIRES_AT_REQUIRED(HttpStatus.BAD_REQUEST, "R004", "임시 예약 만료 시간이 필요합니다."),

    RES_STATE_TRANSITION_NOT_ALLOWED(HttpStatus.CONFLICT, "R005", "예약 상태를 변경할 수 없습니다."),
    RES_ALREADY_EXPIRED(HttpStatus.CONFLICT, "R006", "이미 만료된 예약입니다."),
    RES_NOT_EXPIRED_YET(HttpStatus.CONFLICT, "R007", "아직 만료되지 않은 예약입니다."),
    RES_NOT_ENDED_YET(HttpStatus.CONFLICT, "R008", "아직 이용 종료 시간이 지나지 않았습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
