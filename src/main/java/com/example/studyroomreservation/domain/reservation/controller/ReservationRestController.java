package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.request.ReservationCreateRequest;
import com.example.studyroomreservation.domain.reservation.dto.response.RoomReservableTimeResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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

    @PostMapping
    public ApiResponse<Long> createReservation(
            @RequestBody ReservationCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails){

        Long memberId = (userDetails != null) ? 1L : null;

        log.info("예약 생성 요청 - RoomId: {}, Time: {} ~ {} MemberId: {}",
                request.roomId(), request.startTime(), request.endTime(), memberId);

        Long reservationId = reservationService.createReservation(request, memberId);
        return ApiResponse.success(reservationId);
    }

    @GetMapping("/availability")
    public ApiResponse<List<RoomReservableTimeResponse>> getAvailability(
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
    {
        List<RoomReservableTimeResponse> reservedTimes = reservationService.getReservedTimes(roomId, date);
        return ApiResponse.success(reservedTimes);
    }


}
