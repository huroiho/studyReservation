package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

import static com.example.studyroomreservation.domain.reservation.controller.ReservationConstants.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;


    @GetMapping(VIEW_RESERVATION_BASE + VIEW_RESERVATION_DETAIL)
    public String reservationDetail(@PathVariable("reservationId") Long reservationId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    Model model) {

        Long memberId = userDetails.getMember().getId();
        ReservationDetailResponse response = reservationService.getReservationDetail(reservationId, memberId);
        model.addAttribute("reservationDetail", response);

        return TMPL_RESERVATION_DETAIL;
    }

    @PostMapping(VIEW_RESERVATION_BASE + VIEW_RESERVATION_CANCEL)
    public String cancelReservation(@PathVariable("reservationId") Long reservationId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long memberId = userDetails.getMember().getId();
        reservationService.cancelReservation(reservationId, memberId);

        return REDIRECT_RESERVATION_DETAIL + reservationId;
    }


    // 마이페이지 예약 목록 (기존 ReservationMypageController)
    @GetMapping(VIEW_MY_RESERVATION_BASE + VIEW_MY_RESERVATION_LIST)
    public String getMyReservations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 예약현황
        List<ReservationResponse> responses = reservationService.getMyActiveReservations(userDetails.getMember().getId());
        model.addAttribute("reservations", responses);
        return TMPL_MY_RESERVATION_LIST;
    }

    @GetMapping(VIEW_MY_RESERVATION_BASE + VIEW_MY_RESERVATION_HISTORY)
    public String getHistory(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model
    ) {
        Long memberId = userDetails.getMember().getId();

        Page<ReservationResponse> historyPage = reservationService.getMyReservationHistory(memberId, pageable);

        model.addAttribute("historyPage", historyPage);
        model.addAttribute("reservations", historyPage.getContent());
        model.addAttribute("isHistoryPage", true);

        return TMPL_MY_RESERVATION_HISTORY;
    }
}
