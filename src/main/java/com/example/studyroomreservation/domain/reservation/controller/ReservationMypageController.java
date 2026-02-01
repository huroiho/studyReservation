package com.example.studyroomreservation.domain.reservation.controller;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.domain.reservation.dto.response.ReservationResponse;
import com.example.studyroomreservation.domain.reservation.service.ReservationService;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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
    private final MemberRepository memberRepository;

    @GetMapping("/reservations")
    public String getMyReservations(HttpSession session, Model model) {

        // data.sql 사용 (실제 데이터 생성후 제거)
        if (session.getAttribute("loginMember") == null) {
            Member realMember = memberRepository.findById(1L)
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
            session.setAttribute("loginMember", realMember);
        }

        // 프로필
        Member member = (Member) session.getAttribute("loginMember");
        if (member == null) {
            return "redirect:/login";
        }

        // 예약현황
        List<ReservationResponse> responses = reservationService.getMyActiveReservations(member.getId());

        model.addAttribute("reservations", responses);
        model.addAttribute("userName", member.getName());
        return MY_LIST_VIEW;
    }
}
