package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.example.studyroomreservation.domain.reservation.controller.ReservationControllerConstants.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(RES_MVC_BASE_PATH)
public class ReservationViewController {
    private final ReservationService reservationService;


    @GetMapping(RES_MVC_DETAIL_PATH)
    public String reservationDetail(@PathVariable("reservationId") Long reservationId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {

        Long memberId = userDetails.getMember().getId();
        ReservationDetailResponse response = reservationService.getReservationDetail(reservationId, memberId);
        model.addAttribute("reservationDetail", response);

        return RES_USER_DETAIL;
    }

    @PostMapping(RES_MVC_CANCEL_PATH)
    public String cancelReservation(@PathVariable("reservationId") Long reservationId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        reservationService.cancelReservation(reservationId, memberId);

        return RES_REDIRECT_RES_DETAIL + reservationId;
    }

}