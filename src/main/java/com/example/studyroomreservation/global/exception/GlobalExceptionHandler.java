package com.example.studyroomreservation.global.exception;

import com.example.studyroomreservation.global.util.LogMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@Order(2)
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
            BusinessException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        log.error("[WEB] code={} message={} path={} detail={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI(),
                LogMaskingUtil.mask(e.getDetailMessage()),
                e
        );

        // HTTP 상태코드 맞추기
        response.setStatus(errorCode.getStatus().value());

        // 뷰에서 쓸 값들
        model.addAttribute("code", errorCode.getCode());
        model.addAttribute("message", errorCode.getMessage());
        model.addAttribute("path", request.getRequestURI());

        // 공통 에러 페이지로
        return "error/common";
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model,
                                  HttpServletResponse response, HttpServletRequest request) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        log.error("[WEB] code={} message={} path={}",
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage(),
                request.getRequestURI(),
                e
        );
        response.setStatus(errorCode.getStatus().value());
        model.addAttribute("code", errorCode.getCode());
        model.addAttribute("message", errorCode.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error/common";
    }

}
