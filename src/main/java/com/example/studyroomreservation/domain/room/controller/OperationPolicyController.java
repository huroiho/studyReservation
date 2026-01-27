package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.service.OperationPolicyService;
import com.example.studyroomreservation.domain.room.web.OperationPolicyFormFactory;
import com.example.studyroomreservation.global.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/operation-policies")
public class OperationPolicyController {

    private final OperationPolicyService operationPolicyService;
    private final OperationPolicyFormFactory formFactory;

    @GetMapping("/new")
    public String createForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", formFactory.emptyCreateForm());
        }
        injectCommonModel(model);
        return "room/admin/create-operation-policy";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") OperationPolicyCreateRequest form,
            BindingResult br,
            Model model
    ) {
        System.out.println("=== schedules closed values ===");
        for (int i = 0; i < form.schedules().size(); i++) {
            var s = form.schedules().get(i);
            System.out.println(i + " " + s.dayOfWeek() + " closed=" + s.closed()
                    + " open=" + s.openTime() + " close=" + s.closeTime());
        }

        if (br.hasErrors()) {
            System.out.println("=== validation errors ===");
            br.getAllErrors().forEach(e ->
                    System.out.println(e.getObjectName() + " / " + e.getDefaultMessage())
            );
            br.getFieldErrors().forEach(e ->
                    System.out.println("FIELD: " + e.getField() + " / " + e.getDefaultMessage() + " / rejected=" + e.getRejectedValue())
            );

            injectCommonModel(model);
            return "room/admin/create-operation-policy";
        }

        try {
            Long id = operationPolicyService.create(form);
            return "redirect:/admin/operation-policies/" + id;
        } catch (BusinessException e) {
            br.reject("business", e.getErrorCode().getMessage());
            System.out.println("=== BusinessException errors ===");
            br.getAllErrors().forEach(ex ->
                    System.out.println(ex.getObjectName() + " / " + ex.getDefaultMessage())
            );
            br.getFieldErrors().forEach(ex ->
                    System.out.println("FIELD: " + ex.getField() + " / " + ex.getDefaultMessage() + " / rejected=" + ex.getRejectedValue())
            );
            // 폼 유지가 필요한 비즈니스 에러는 여기서 처리
            injectCommonModel(model);
            return "room/admin/create-operation-policy";
        }
    }

    private void injectCommonModel(Model model) {
        model.addAttribute("slotUnits", formFactory.slotUnits());
        model.addAttribute("hours", formFactory.hourOptions());
        model.addAttribute("days", formFactory.orderedDays());
    }
}
