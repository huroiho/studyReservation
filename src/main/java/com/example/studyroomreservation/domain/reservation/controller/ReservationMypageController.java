package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.response.ReservationResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static com.example.studyroomreservation.domain.reservation.controller.ReservationControllerConstants.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(MY_BASE_PATH)
public class ReservationMypageController {
    private final ReservationService reservationService;

    @GetMapping("/reservations")
    public String getMyReservations(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 예약현황
        List<ReservationResponse> responses = reservationService.getMyActiveReservations(userDetails.getMember().getId());
        model.addAttribute("reservations", responses);
        return MY_LIST_VIEW;
    }

    // 예약현황 테스트용 data.sql 사용 (실제 데이터 생성후 제거)
//    public String getMyReservations(Model model) {
//        Long testMemberId = 1L;
//        List<ReservationResponse> responses = reservationService.getMyActiveReservations(testMemberId);
//        model.addAttribute("reservations", responses);
//        return MY_LIST_VIEW;
//    }

    @GetMapping("/history")
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

        return MY_HIS_LIST_VIEW;
    }

    // 예약 히스토리 테스트용 data.sql 사용 (실제 데이터 생성후 제거)
//    public String getHistory(@PageableDefault(size = 10) Pageable pageable, Model model) {
//        Long testMemberId = 1L;
//
//        Page<ReservationResponse> historyPage = reservationService.getMyReservationHistory(testMemberId, pageable);
//
//        model.addAttribute("historyPage", historyPage);
//        model.addAttribute("reservations", historyPage.getContent());
//        model.addAttribute("isHistoryPage", true);
//
//        return MY_HIS_LIST_VIEW;
//    }

}
