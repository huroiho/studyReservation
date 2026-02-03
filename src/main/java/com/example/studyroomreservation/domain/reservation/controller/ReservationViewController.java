package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.response.AdminReservationResponse;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import com.example.studyroomreservation.global.security.auth.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/{reservationId}/cancel")
    public String cancelReservation(@PathVariable Long reservationId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        reservationService.cancelReservation(reservationId, memberId);

        return "redirect:/reservations/" + reservationId;
    }



}
