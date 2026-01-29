package com.example.studyroomreservation.global.exception;

import com.example.studyroomreservation.global.util.LogMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
     * [수정] AJAX 요청 여부를 판단하여 JSON 또는 HTML로 응답합니다.
     */
    @ExceptionHandler(BusinessException.class)
    public Object handleBusinessException(
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

        // 비동기(AJAX) 요청인지 확인
        if (isAjaxRequest(request)) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity
                    .status(errorCode.getStatus())
                    .body(apiErrorResponse);
        }

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
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(
            NoResourceFoundException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        log.warn("[404 Not Found] path={}", request.getRequestURI());
        return render404(model, response, request);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNoHandlerFound(
            NoHandlerFoundException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        log.warn("[404 Not Found] path={}", request.getRequestURI());
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
     * DB 제약조건 위반 처리
     * - Unique 제약 위반 (race condition으로 Validator 통과 후 발생)
     * - FK 제약 위반 등
     * - 시스템 이상으로 처리하되, 별도 로깅으로 디버깅 용이하게 함
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrityViolation(
            DataIntegrityViolationException e,
            Model model,
            HttpServletResponse response,
            HttpServletRequest request
    ) {
        log.error("[DB Constraint Violation] path={} message={}",
                request.getRequestURI(),
                e.getMostSpecificCause().getMessage(),
                e
        );

        response.setStatus(HttpStatus.CONFLICT.value());
        model.addAttribute("errorCode", "E409");
        model.addAttribute("errorMessage", "데이터 처리 중 충돌이 발생했습니다. 다시 시도해주세요.");
        model.addAttribute("statusCode", 409);
        model.addAttribute("path", request.getRequestURI());

        return "error/common";
    }

    /**
     * 예상치 못한 시스템 예외 처리
     * - NullPointerException, IllegalStateException 등
     * - 개발자에게 알려야 하는 심각한 에러
     * [수정] 시스템 예외 발생 시에도 AJAX 요청이면 JSON을 반환하도록 보완 가능
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(
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

        if (isAjaxRequest(request)) {
            ApiErrorResponse apiErrorResponse = new ApiErrorResponse(
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    request.getRequestURI()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiErrorResponse);
        }

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

    /**
     * [추가] AJAX 요청 여부 판별 유틸리티
     */
    private boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/json");
    }

}