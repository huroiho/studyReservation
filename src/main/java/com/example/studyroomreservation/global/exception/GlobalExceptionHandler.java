package com.example.studyroomreservation.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
            BusinessException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        // HTTP 상태코드 맞추기
        response.setStatus(errorCode.getStatus().value());

        // 뷰에서 쓸 값들
        model.addAttribute("code", errorCode.getCode());
        model.addAttribute("message", errorCode.getMessage());
        model.addAttribute("path", request.getRequestURI());

        // 공통 에러 페이지로
        return "error/common";
    }
}
