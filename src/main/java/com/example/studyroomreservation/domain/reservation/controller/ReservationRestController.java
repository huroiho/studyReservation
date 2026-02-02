package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.common.ApiResponse;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationRestController {
    private final ReservationService reservationService;

    // TODO: 예약자 정보(reserver fields) 처리 정책 논의 필요
    // - 로그인 사용자 정보로 자동 설정할지
    // - 예약 시점에 별도의 예약자(쇼핑몰에서 수취인) 정보를 입력받을지
    // 현재는 와이어프레임 기준으로 화면만 구현되어 있으며,
    // API 전송 및 도메인 반영은 정책 결정 후 진행 예정
    @PostMapping
    public ApiResponse<Long> createReservation(
            @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails){

        // FIXME: 로그인 미완료 - 예약 생성 임시 차단
        // - memberId를 추출할 수 없어 잘못된 데이터 생성 방지를 위해 차단함
        // - 로그인 통합 완료 후 userDetails에서 memberId 추출 필요
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING, "로그인이 필요합니다.");
        }
        // 로그인되어 있어도 memberId 추출 안됨
        throw new BusinessException(ErrorCode.RES_REQUIRED_VALUE_MISSING,
                "memberId 추출 필요");
        /*
        Long memberId = (userDetails != null) ? 1L : null;

        log.info("예약 생성 요청 - RoomId: {}, Time: {} ~ {} MemberId: {}",
                request.roomId(), request.startTime(), request.endTime(), memberId);

        Long reservationId = reservationService.createReservation(request, memberId);
        return ApiResponse.success(reservationId);
        */
    }

}
