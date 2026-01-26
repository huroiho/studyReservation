package com.example.studyroomreservation.domain.room.cotroller;

import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/roomRules")
public class RoomRuleController {
    private final RoomRuleService roomRuleService;

    // 전체 목록
    @GetMapping
    public String getAllRules(Model model) {
        List<RoomRuleResponse> roomRules = roomRuleService.getAllRules();
        model.addAttribute("roomRules", roomRules);
        return "room/admin/roomRuleList";
    }

    // 활성화 목록 조회 별도x -> RoomController 에서 getActiveRules 호출예정

    // 상세 조회
    @GetMapping("/{id}")
    public String getRuleDetail(@PathVariable Long id, Model model) {
          //상세화면 확인용
//        RoomRuleResponse dummy = new RoomRuleResponse(
//                id,                         // id
//                "일반 예약 규칙",              // name
//                60,                         // minDurationMinutes
//                30,                         // bookingOpenDays
//                true,                       // isActive
//                LocalDateTime.now(),        // createdAt
//                LocalDateTime.now()         // activeUpdatedAt (DTO 필드명 확인)
//        );

        RoomRuleResponse roomRules = roomRuleService.getRuleDetail(id);
        model.addAttribute("roomRules", roomRules);
        return "room/admin/roomRuleDetail"; // templates/roomRuleDetail.html
    }
}
