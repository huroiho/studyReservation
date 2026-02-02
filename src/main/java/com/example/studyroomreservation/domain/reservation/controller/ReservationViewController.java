package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import com.example.studyroomreservation.global.security.auth.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String reservationDetail(@PathVariable Long reservationId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {

        Long memberId = userDetails.getMember().getId();

        ReservationDetailResponse response = reservationService.getReservationDetail(reservationId, memberId);

        model.addAttribute("reservationDetail", response);

        return "reservation/reservation-detail";
    }
}
