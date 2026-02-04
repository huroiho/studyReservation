package com.example.studyroomreservation.global.exception;

import com.example.studyroomreservation.global.util.LogMaskingUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@Slf4j
@Order(1)
@RestControllerAdvice(annotations = RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            BusinessException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();

        log.error(
                "[API] code={} message={} path={} detail={}",
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI(),
                LogMaskingUtil.mask(e.getDetailMessage()),
                e
        );

        ApiErrorResponse body = new ApiErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(body);
    }

    /**
     * @Valid on @ModelAttribute 검증 실패
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(
            BindException e,
            HttpServletRequest request
    ) {
        String fieldErrorMessage = extractFirstFieldErrorMessage(e);

        log.warn("[API Validation] path={} errors={}",
                request.getRequestURI(), fieldErrorMessage);

        ApiErrorResponse body = new ApiErrorResponse(
                ErrorCode.INVALID_REQUEST.getCode(),
                fieldErrorMessage,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * @Valid on @RequestBody 검증 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpServletRequest request
    ) {
        String fieldErrorMessage = extractFirstFieldErrorMessage(e);

        log.warn("[API Validation] path={} errors={}",
                request.getRequestURI(), fieldErrorMessage);

        ApiErrorResponse body = new ApiErrorResponse(
                ErrorCode.INVALID_REQUEST.getCode(),
                fieldErrorMessage,
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * multipart 요청에서 특정 part가 누락된 경우
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingServletRequestPart(
            MissingServletRequestPartException e,
            HttpServletRequest request
    ) {
        ErrorCode code = "mainImage".equals(e.getRequestPartName())
                ? ErrorCode.ROOM_MAIN_IMAGE_REQUIRED
                : ErrorCode.INVALID_REQUEST;

        log.warn("[API] Missing request part: partName={}, path={}",
                e.getRequestPartName(), request.getRequestURI());

        ApiErrorResponse body = new ApiErrorResponse(
                code.getCode(),
                code.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * multipart 파싱 실패 (Content-Type/boundary 문제 등)
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiErrorResponse> handleMultipartException(
            MultipartException e,
            HttpServletRequest request
    ) {
        log.warn("[API] Multipart parse failed: path={}, msg={}",
                request.getRequestURI(), e.getMessage());

        ApiErrorResponse body = new ApiErrorResponse(
                ErrorCode.INVALID_REQUEST.getCode(),
                "multipart 요청 형식이 올바르지 않습니다.",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    /**
     * Spring multipart 파일 크기 제한 초과
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException e,
            HttpServletRequest request
    ) {
        log.warn("[API] File upload size exceeded: path={}", request.getRequestURI());

        ApiErrorResponse body = new ApiErrorResponse(
                ErrorCode.ROOM_IMAGE_SIZE_EXCEEDED.getCode(),
                ErrorCode.ROOM_IMAGE_SIZE_EXCEEDED.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception e,
            HttpServletRequest request
    ) {
        log.error("[API] code={} message={} path={}",
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage(),
                request.getRequestURI(),
                e
        );

        ApiErrorResponse body = new ApiErrorResponse(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getStatus())
                .body(body);
    }

    private String extractFirstFieldErrorMessage(BindException e) {
        return e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .filter(msg -> msg != null)
                .findFirst()
                .orElse(ErrorCode.INVALID_REQUEST.getMessage());
    }
}
