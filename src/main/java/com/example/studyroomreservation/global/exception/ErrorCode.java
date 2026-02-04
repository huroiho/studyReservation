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
    MEMBER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "M002", "이미 사용 중인 이메일입니다."),
    MEMBER_PHONE_DUPLICATED(HttpStatus.CONFLICT, "M003", "이미 사용 중인 전화번호입니다."),
    MEMBER_NAME_BLANK(HttpStatus.BAD_REQUEST, "M004", "이름을 입력해주세요."),
    MEMBER_PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "M005", "새 비밀번호 확인이 일치하지 않습니다."),
    MEMBER_PASSWORD_INVALID(HttpStatus.BAD_REQUEST, "M006", "현재 비밀번호가 올바르지 않습니다."),

    //ROOM
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "RM001","해당 방이 존재하지 않습니다." ),
    ROOM_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "RM002", "해당 방은 현재 이용할 수 없습니다."),
    ROOM_INVALID_PAST_DATE(HttpStatus.BAD_REQUEST, "RM003", "과거 날짜는 조회할 수 없습니다."),

    ROOM_MAIN_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "RM004", "메인 이미지는 필수입니다."),
    ROOM_IMAGE_TYPE_INVALID(HttpStatus.BAD_REQUEST, "RM005", "이미지는 jpg, png, webp 형식만 가능합니다."),
    ROOM_IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "RM006", "이미지 크기는 5MB를 초과할 수 없습니다."),
    ROOM_IMAGE_DIMENSION_EXCEEDED(HttpStatus.BAD_REQUEST, "RM007", "이미지 크기는 8000x8000 픽셀을 초과할 수 없습니다."),
    ROOM_GENERAL_IMAGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "RM008", "일반 이미지는 최대 10개까지 등록 가능합니다."),
    ROOM_IMAGE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RM009", "이미지 저장에 실패했습니다."),
    ROOM_THUMBNAIL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "RM010", "썸네일 생성에 실패했습니다."),

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
    REF_POLICY_INACTIVE(HttpStatus.BAD_REQUEST, "RF010","비활성화된 환불 정책입니다"),

    // RESERVATION
    RES_REQUIRED_VALUE_MISSING(HttpStatus.BAD_REQUEST, "RS001", "필수 값이 누락되었습니다."),
    RES_INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "RS002", "예약 시간이 올바르지 않습니다."),
    RES_NEGATIVE_AMOUNT(HttpStatus.BAD_REQUEST, "RS003", "결제 금액이 올바르지 않습니다."),
    RES_TEMP_EXPIRES_AT_REQUIRED(HttpStatus.BAD_REQUEST, "RS004", "임시 예약 만료 시간이 필요합니다."),
    RES_PAST_TIME_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RS005", "과거 시간은 예약할 수 없습니다."),
    RES_OUT_OF_OPERATION_HOURS(HttpStatus.BAD_REQUEST, "RS006", "운영 시간 내에서만 예약 가능합니다."),
    RES_CROSS_DAY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "RS007", "예약은 하루 단위로만 가능합니다."),
    RES_MIN_DURATION_NOT_MET(HttpStatus.BAD_REQUEST, "RS008", "최소 이용 시간을 준수해야 합니다."),
    RES_BOOKING_PERIOD_EXCEEDED(HttpStatus.BAD_REQUEST, "RS009", "예약 가능 기간을 초과했습니다."),
    RESERVATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "RS010", "해당 예약이 존재하지 않습니다."),

    RES_STATE_TRANSITION_NOT_ALLOWED(HttpStatus.CONFLICT, "RS011", "예약 상태를 변경할 수 없습니다."),
    RES_ALREADY_EXPIRED(HttpStatus.CONFLICT, "RS012", "이미 만료된 예약입니다."),
    RES_NOT_EXPIRED_YET(HttpStatus.CONFLICT, "RS013", "아직 만료되지 않은 예약입니다."),
    RES_NOT_ENDED_YET(HttpStatus.CONFLICT, "RS014", "아직 이용 종료 시간이 지나지 않았습니다."),
    RES_ALREADY_RESERVED(HttpStatus.CONFLICT, "RS015", "이미 예약된 시간입니다."),
    RES_CANCEL_NOT_ALLOWED(HttpStatus.CONFLICT,"R016","취소 가능한 상태나 시간이 아닙니다."),

    //PAYMENT
    PAYMENT_STATUS_INVALID_TRANSITION(HttpStatus.CONFLICT,"P001", "결제 상태를 변경할 수 없습니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "P002", "결제 금액이 일치하지 않습니다."),
    PAYMENT_DUPLICATE_APPROVE(HttpStatus.CONFLICT,"P003","이미 결제가 성공 처리된 예약입니다."),
    PAYMENT_ATTEMPT_NOT_FOUND(HttpStatus.NOT_FOUND,"P004","처리할 결제 시도가 존재하지 않습니다."),
    PAYMENT_CONFLICT(HttpStatus.CONFLICT, "P005", "결제 처리 중 충돌이 발생했습니다."),
    PAYMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "P008", "결제 처리에 실패했습니다."),
    PAYMENT_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "P009", "잘못된 결제 요청입니다."),
    PAYMENT_IN_PROGRESS(HttpStatus.CONFLICT, "P010", "이미 결제가 처리 중입니다."),

    // OPERATION POLICY
    OP_POLICY_SCHEDULE_REQUIRED(HttpStatus.BAD_REQUEST, "OP001", "요일별 운영 스케줄이 필요합니다."),
    OP_POLICY_SCHEDULE_NOT_7DAYS(HttpStatus.BAD_REQUEST, "OP002", "운영 스케줄은 월~일 7일 모두 필요합니다."),
    OP_POLICY_DAY_REQUIRED(HttpStatus.BAD_REQUEST, "OP003", "요일 정보는 필수입니다."),
    OP_POLICY_DAY_DUPLICATED(HttpStatus.BAD_REQUEST, "OP004", "요일별 운영 스케줄이 중복되었습니다."),
    OP_POLICY_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "OP005", "운영 정책 이름은 필수입니다."),
    OP_SLOT_UNIT_REQUIRED(HttpStatus.BAD_REQUEST, "OP006", "슬롯 단위는 필수입니다."),
    OP_POLICY_NAME_DUPLICATE(HttpStatus.BAD_REQUEST, "OP007", "이미 존재하는 정책 이름입니다."),
    OP_POLICY_NOT_FOUND(HttpStatus.NOT_FOUND, "OP008", "운영 정책이 존재하지 않습니다."),
    OP_POLICY_IN_USE_BY_ROOM(HttpStatus.CONFLICT, "OP009", "이 정책을 사용 중인 룸이 있어 삭제할 수 없습니다."),
    OP_POLICY_IN_USE_BY_RESERVATION(HttpStatus.CONFLICT, "OP010", "이 정책이 적용된 예약이 있어 삭제할 수 없습니다."),
    OP_POLICY_INACTIVE(HttpStatus.BAD_REQUEST, "OP011","비활성화된 운영 정책입니다"),

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

    // ROOM RULE
    RR_NAME_DUPLICATE(HttpStatus.CONFLICT, "RR001", "이미 존재하는 규칙 이름입니다."),
    RR_VALUES_DUPLICATE(HttpStatus.CONFLICT, "RR002", "동일한 설정을 가진 예약 규칙이 이미 존재합니다."),
    RR_INVALID_REQUEST(HttpStatus.BAD_REQUEST, "RR003", "최소 1개의 이용 규칙은 활성화되어 있어야 합니다."),
    RR_IN_USE(HttpStatus.BAD_REQUEST, "RR004", "현재 객실에서 사용 중인 규칙은 비활성화할 수 없습니다."),
    RR_NOT_FOUND(HttpStatus.NOT_FOUND, "RR005", "예약 규칙이 존재하지 않습니다."),
    RR_INACTIVE(HttpStatus.BAD_REQUEST, "RR006","비활성화된 예약 규칙입니다");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
