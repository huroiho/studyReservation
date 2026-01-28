package com.example.studyroomreservation.global.exception;

import com.example.studyroomreservation.global.util.LogMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@Order(2)
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     * - 중복 등록, 권한 없음, 데이터 없음 등의 예상 가능한 에러
     * - 사용자에게 적절한 에러 메시지를 보여줌
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(
            BusinessException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[Business Error] code={} message={} path={} detail={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI(),
                LogMaskingUtil.mask(e.getDetailMessage())
        );

        // HTTP 상태 코드 설정
        response.setStatus(errorCode.getStatus().value());

        // 에러 페이지에 전달할 데이터
        model.addAttribute("errorCode", errorCode.getCode());
        model.addAttribute("errorMessage", errorCode.getMessage());
        model.addAttribute("statusCode", errorCode.getStatus().value());
        model.addAttribute("path", request.getRequestURI());

        return "error/common";
    }

    /**
     * 404 Not Found 처리
     * - 존재하지 않는 URL 접근 시
     */
    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class
    })
    public String handleNoResourceFound(
            NoResourceFoundException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        log.warn("[404 Not Found - NoResource] path={}", request.getRequestURI());

        return render404(model, response, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public String handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        log.warn("[405 Method Not Allowed] method={} path={} supported={}",
                request.getMethod(),
                request.getRequestURI(),
                e.getSupportedHttpMethods()
        );

        response.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
        model.addAttribute("errorCode", "E405");
        model.addAttribute("errorMessage", "허용되지 않는 요청 방식입니다.");
        model.addAttribute("statusCode", 405);
        model.addAttribute("path", request.getRequestURI());

        return "error/common";
    }

    /**
     * 예상치 못한 시스템 예외 처리
     * - NullPointerException, IllegalStateException 등
     * - 개발자에게 알려야 하는 심각한 에러
     */
    @ExceptionHandler(Exception.class)
    public String handleException(
            Exception e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;

        // 스택 트레이스까지 로깅 (심각한 에러이므로)
        log.error("[System Error] code={} message={} path={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI(),
                e  // 스택 트레이스 포함
        );

        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());

        model.addAttribute("errorCode", errorCode.getCode());
        model.addAttribute("errorMessage", errorCode.getMessage());
        model.addAttribute("statusCode", 500);
        model.addAttribute("path", request.getRequestURI());

        return "error/common";
    }

    private String render404(
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("errorCode", "E404");
        model.addAttribute("errorMessage", "요청하신 페이지를 찾을 수 없습니다.");
        model.addAttribute("statusCode", 404);
        model.addAttribute("path", request.getRequestURI());
        return "error/common";
    }

}
