package com.example.studyroomreservation.global.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());   // RuntimeException 메시지로도 세팅
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);            // 필요하면 상세 메시지로 덮어쓰기
        this.errorCode = errorCode;
    }
}
