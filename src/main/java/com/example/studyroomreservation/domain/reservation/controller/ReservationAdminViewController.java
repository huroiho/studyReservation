package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.reservation.dto.response.AdminReservationResponse;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationDetailResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.example.studyroomreservation.domain.reservation.controller.ReservationControllerConstants.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(ADMIN_BASE_PATH)
@PreAuthorize("hasRole('ADMIN')")
public class ReservationAdminViewController {

    private final ReservationService reservationService;

    @GetMapping
    public String adminReservationList(
            Model model,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminReservationResponse> reservations = reservationService.getAllReservationsForAdmin(pageable);

        model.addAttribute("reservations", reservations);
        model.addAttribute("maxPage", 10);

        return RES_ADMIN_LIST;
    }

    @GetMapping(ADMIN_DETAIL_PATH)
    public String adminReservationDetail(@PathVariable("reservationId") Long reservationId, Model model) {
        ReservationDetailResponse detail = reservationService.getReservationDetailForAdmin(reservationId);

        model.addAttribute("reservation", detail);

        return RES_ADMIN_DETAIL;
    }
}