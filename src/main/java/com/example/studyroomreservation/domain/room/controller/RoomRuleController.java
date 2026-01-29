package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.request.RoomRuleCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import com.example.studyroomreservation.domain.room.validation.RoomRuleValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/roomrules")
public class RoomRuleController {
    private final RoomRuleService roomRuleService;
    private final RoomRuleValidator roomRuleValidator;


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
        RoomRuleResponse roomRules = roomRuleService.getRuleDetail(id);
        model.addAttribute("roomRules", roomRules);

        return "room/admin/roomrule-detail";
    }

    @InitBinder("roomRuleRequest")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(roomRuleValidator);
    }

    // 등록 폼 (룸 규칙은 수정불가)
    @GetMapping("/new")
    public String showCreateForm(Model model){
        model.addAttribute("roomRuleRequest", new RoomRuleCreateRequest("", 0, 0, true));
        return "room/admin/roomrule-form";
    }

    // 저장처리
    @PostMapping
    public String create(@ModelAttribute("roomRuleRequest") @Valid RoomRuleCreateRequest request, BindingResult result) {
        roomRuleValidator.validate(request, result);
        if (result.hasErrors()) {
            return "room/admin/roomrule-form";
        }
        roomRuleService.createRoomRule(request);
        return "redirect:/admin/roomrules";
    }
}
