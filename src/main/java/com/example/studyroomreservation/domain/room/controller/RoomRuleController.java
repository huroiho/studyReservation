package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/roomrules")
public class RoomRuleController {
    private final RoomRuleService roomRuleService;

    // 전체 목록
    @GetMapping
    public String getAllRules(Model model, @RequestParam(value = "page", defaultValue = "0") int page) {
        model.addAttribute("paging", roomRuleService.getAllRoomRules(page));
        model.addAttribute("currentUrl", "admin/roomrules");
        return "room/admin/roomrule-list";
    }

    // 활성화 목록 조회 별도x -> RoomController 에서 getActiveRoomRules 호출예정

    // 상세 조회
    @GetMapping("/{id}")
    public String getRuleDetail(@PathVariable Long id, Model model) {

        // [테스트 단계] 화면 확인용 더미 데이터 직접 생성
//        RoomRuleResponse dummy = new RoomRuleResponse(
//                id,                         // id
//                "테스트용 일반 예약 규칙",      // name
//                60,                         // minDurationMinutes
//                30,                         // bookingOpenDays
//                true,                       // isActive
//                LocalDateTime.now(),        // createdAt
//                LocalDateTime.now()         // activeUpdatedAt
//        );
//
//        // 실제 화면에서 사용할 변수명 'roomRules'에 더미 데이터를 담음
//        model.addAttribute("roomRules", dummy);

        RoomRuleResponse roomRules = roomRuleService.getRuleDetail(id);
        return "room/admin/roomrule-detail";
    }
}
