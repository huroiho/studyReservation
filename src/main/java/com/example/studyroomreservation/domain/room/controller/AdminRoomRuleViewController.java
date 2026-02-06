package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.request.RoomRuleCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.RoomRuleResponse;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import com.example.studyroomreservation.domain.room.validation.RoomRuleValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import static com.example.studyroomreservation.domain.room.controller.RoomRuleControllerConstants.*;


@Controller
@RequiredArgsConstructor
@RequestMapping(BASE_PATH)
public class AdminRoomRuleViewController {
    private final RoomRuleService roomRuleService;
    private final RoomRuleValidator roomRuleValidator;


    // 전체 목록
    @GetMapping
    public String getAllRules(Model model, @RequestParam(value = "page", defaultValue = "0") int page, HttpServletResponse response) {
        // 상세에서 상태변경 후 뒤로가기 클릭시 상태값 반영 (브라우저 캐시를 사용하지 않도록 설정)
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxies

        model.addAttribute(PAGING, roomRuleService.getAllRoomRules(page));
        model.addAttribute(CURRENT_URL, BASE_PATH);
        return LIST_VIEW;
    }

    // 활성화 목록 조회 별도x -> RoomController 에서 getActiveRoomRules 호출예정

    // 상세 조회
    @GetMapping("/{id}")
    public String getRuleDetail(@PathVariable Long id, Model model) {
        RoomRuleResponse roomRules = roomRuleService.getRuleDetail(id);
        model.addAttribute(ROOMRULES, roomRules);

        return DETAIL_VIEW;
    }

    @InitBinder("roomRuleRequest")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(roomRuleValidator);
    }

    // 등록 폼 (룸 규칙은 수정불가)
    @GetMapping(NEW_FORM_PATH)
    public String showCreateForm(Model model){
        model.addAttribute(ROOMRULE_REQUEST, new RoomRuleCreateRequest("", 0, 0, true));
        return FORM_VIEW;
    }

    // 저장처리
    @PostMapping
    public String create(@ModelAttribute(ROOMRULE_REQUEST) @Valid RoomRuleCreateRequest request, BindingResult result) {
        roomRuleValidator.validate(request, result);
        if (result.hasErrors()) {
            return FORM_VIEW;
        }
        roomRuleService.createRoomRule(request);
        return REDIRECT_BASE;
    }

    // 상태변경
//    @PatchMapping("/{id}/status")
//    @ResponseBody
//    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
//            roomRuleService.updateStatus(id, active);
//            return ResponseEntity.ok().build();
//    }
}
