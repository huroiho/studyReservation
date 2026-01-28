package com.example.studyroomreservation.global.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring Boot 기본 에러 처리(/error)를 대체하는 커스텀 컨트롤러.
 * Dispatcher 이전 단계나 처리되지 않은 예외로 /error에 도달한 요청을
 * 공통 에러 페이지(error/common)로 렌더링한다.
 */
@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = (status != null) ? (Integer) status : 500;
        String path = (uri != null) ? uri.toString() : "unknown";

        // 예외가 있으면 로깅
        if (exception != null) {
            log.error("[ErrorController] status={} path={}", statusCode, path, (Throwable) exception);
        } else {
            log.warn("[ErrorController] status={} path={} message={}", statusCode, path, message);
        }

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("errorCode", "E" + statusCode);
        model.addAttribute("errorMessage", getErrorMessage(statusCode, message));
        model.addAttribute("path", path);

        return "error/common";
    }

    private String getErrorMessage(int statusCode, Object message) {
        // 의미 있는 메시지가 있으면 사용, 없으면 기본 메시지
        if (message != null && !message.toString().isBlank()
                && !"No message available".equals(message.toString())) {
            return message.toString();
        }

        return switch (statusCode) {
            case 400 -> "잘못된 요청입니다.";
            case 401 -> "로그인이 필요합니다.";
            case 403 -> "접근 권한이 없습니다.";
            case 404 -> "요청하신 페이지를 찾을 수 없습니다.";
            case 405 -> "허용되지 않는 요청 방식입니다.";
            case 408 -> "요청 시간이 초과되었습니다.";
            case 500 -> "서버 내부 오류가 발생했습니다.";
            case 502 -> "서버 연결에 실패했습니다.";
            case 503 -> "서비스를 일시적으로 사용할 수 없습니다.";
            default -> "오류가 발생했습니다.";
        };
    }
}
