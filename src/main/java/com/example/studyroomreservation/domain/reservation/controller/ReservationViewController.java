package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationViewController {
    private final ReservationService reservationService;

    @GetMapping("/{reservationId}")
    public String reservationDetail(@PathVariable Long reservationId, Model model) {

        // TODO: 추후 Spring Security 적용 시 @AuthenticationPrincipal로 변경 필요
        // 임시 테스트용 회원 ID
        Long memberId = 1L;

        ReservationDetailResponse response = reservationService.getReservationDetail(reservationId, memberId);

        model.addAttribute("reservationDetail", response);

        return "reservation/reservation-detail";
    }
}
