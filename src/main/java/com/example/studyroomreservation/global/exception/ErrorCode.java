package com.example.studyroomreservation.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // COMMON
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "알 수 없는 오류가 발생했습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C002", "잘못된 요청입니다."),

    //MEMBER
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "해당 회원이 존재하지 않습니다."),


    //ROOM
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "R001","해당 방이 존재하지 않습니다." ),

    // REFUND
    REF_POLICY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "RF001", "정책 이름은 필수입니다."),
    REF_RULE_REQUIRED(HttpStatus.BAD_REQUEST, "RF002", "최소 하나의 환불 규칙이 필요합니다."),
    REF_RATE_INVALID(HttpStatus.BAD_REQUEST, "RF003", "환불 비율은 0~100 사이여야 합니다."),
    REF_RULE_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "RF004", "규칙 이름은 필수입니다."),
    REF_POLICY_ALREADY_ASSIGNED(HttpStatus.BAD_REQUEST, "RF005", "이미 환불 정책이 할당되어 있습니다."),
    REF_PAYMENT_REQUIRED(HttpStatus.BAD_REQUEST, "RF006", "환불 대상 결제 정보는 필수입니다."),
    REF_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "RF007", "환불 금액은 0원보다 커야 합니다."),
    REF_POLICY_NAME_DUPLICATE(HttpStatus.CONFLICT, "RF008", "이미 존재하는 정책 이름입니다."),
    REF_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "RF009", "환불 정책이 존재하지 않습니다."),


    // RESERVATION
    RES_REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "R001", "필수 값이 누락되었습니다."),
    RES_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "R002", "예약 시간이 올바르지 않습니다."),
    RES_NEGATIVE_AMOUNT(HttpStatus.BAD_REQUEST, "R003", "결제 금액이 올바르지 않습니다."),
    RES_TEMP_EXPIRES_AT_REQUIRED(HttpStatus.BAD_REQUEST, "R004", "임시 예약 만료 시간이 필요합니다."),

    RES_STATE_TRANSITION_NOT_ALLOWED(HttpStatus.CONFLICT, "R005", "예약 상태를 변경할 수 없습니다."),
    RES_ALREADY_EXPIRED(HttpStatus.CONFLICT, "R006", "이미 만료된 예약입니다."),
    RES_NOT_EXPIRED_YET(HttpStatus.CONFLICT, "R007", "아직 만료되지 않은 예약입니다."),
    RES_NOT_ENDED_YET(HttpStatus.CONFLICT, "R008", "아직 이용 종료 시간이 지나지 않았습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "R009", "해당 예약이 존재하지 않습니다."),
    RES_MIN_DURATION_NOT_MET(HttpStatus.BAD_REQUEST, "R010", "최소 이용 시간을 준수해야 합니다."),
    RES_BOOKING_PERIOD_EXCEEDED(HttpStatus.BAD_REQUEST, "R011", "예약 가능 기간을 초과했습니다."),
    RES_OUT_OF_OPERATION_HOURS(HttpStatus.BAD_REQUEST, "R012", "운영 시간 내에서만 예약 가능합니다."),
    RES_CROSS_DAY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "R013", "예약은 하루 단위로만 가능합니다."),
    RES_ALREADY_RESERVED(HttpStatus.BAD_REQUEST, "R014", "예약은 하루 단위로만 가능합니다."),

    //PAYMENT
    PAYMENT_STATUS_INVALID_TRANSITION(HttpStatus.CONFLICT,"P001", "결제 상태를 변경할 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "P002", "결제 금액이 일치하지 않습니다."),

    // OPERATION POLICY
    OP_POLICY_SCHEDULE_REQUIRED(HttpStatus.BAD_REQUEST, "OP001", "요일별 운영 스케줄이 필요합니다."),
    OP_POLICY_SCHEDULE_NOT_7DAYS(HttpStatus.BAD_REQUEST, "OP002", "운영 스케줄은 월~일 7일 모두 필요합니다."),
    OP_POLICY_DAY_REQUIRED(HttpStatus.BAD_REQUEST, "OP003", "요일 정보는 필수입니다."),
    OP_POLICY_DAY_DUPLICATED(HttpStatus.BAD_REQUEST, "OP004", "요일별 운영 스케줄이 중복되었습니다."),
    OP_POLICY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "OP005", "운영 정책 이름은 필수입니다."),
    OP_SLOT_UNIT_REQUIRED(HttpStatus.BAD_REQUEST, "OP006", "슬롯 단위는 필수입니다."),
    OP_POLICY_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "OP007", "이미 존재하는 정책 이름입니다."),


    // OPERATION SCHEDULE
    OS_POLICY_REQUIRED(HttpStatus.BAD_REQUEST, "OS001", "운영 스케줄은 운영 정책에 속해야 합니다."),
    OS_DAY_REQUIRED(HttpStatus.BAD_REQUEST, "OS002", "운영 스케줄의 요일 정보는 필수입니다."),
    OS_TIME_REQUIRED(HttpStatus.BAD_REQUEST, "OS003", "운영일에는 오픈 및 마감 시간이 필요합니다."),
    OS_TIME_ORDER_INVALID(HttpStatus.BAD_REQUEST, "OS004", "오픈 시간은 마감 시간보다 빨라야 합니다."),
    OS_HOUR_ONLY(HttpStatus.BAD_REQUEST, "OS005", "운영 시간은 정각 단위로만 설정할 수 있습니다."),
    OS_NOT_ALIGNED_TO_SLOT(HttpStatus.BAD_REQUEST, "OS006", "운영 시간은 슬롯 단위로 나누어 떨어져야 합니다."),
    OS_CLOSED_TIME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "OS007", "휴무일에는 운영 시간을 설정할 수 없습니다."),
    OS_DAY_NOT_FOUND(HttpStatus.BAD_REQUEST, "OS008", "해당 요일의 운영 스케줄이 존재하지 않습니다."),
    OS_CLOSED_DAY(HttpStatus.BAD_REQUEST, "OS009", "선택하신 날짜는 휴무일입니다."),

    // ROOM
    RR_NAME_DUPLICATE(HttpStatus.CONFLICT, "RR001", "이미 존재하는 규칙 이름입니다."),
    RR_VALUES_DUPLICATE(HttpStatus.CONFLICT, "RR002", "동일한 설정을 가진 예약 규칙이 이미 존재합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
