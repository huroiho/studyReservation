package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.service.OperationPolicyService;
import com.example.studyroomreservation.domain.room.web.OperationPolicyFormFactory;
import com.example.studyroomreservation.global.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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
        model.addAttribute("form", formFactory.emptyCreateForm());
        injectCommonModel(model);
        return "room/operation-policy/create";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("form") OperationPolicyCreateRequest form,
            BindingResult br,
            Model model
    ) {

        if (br.hasErrors()) {
            injectCommonModel(model);
            return "room/operation-policy/create";
        }

        try {
            Long id = operationPolicyService.create(form);
            return "redirect:/admin/operation-policies/" + id;
        } catch (BusinessException e) {
            br.reject("business", e.getErrorCode().getMessage());
            injectCommonModel(model);
            return "room/operation-policy/create";
        }
    }

    private void injectCommonModel(Model model) {
        model.addAttribute("slotUnits", formFactory.slotUnits());
        model.addAttribute("hours", formFactory.hourOptions());
        model.addAttribute("days", formFactory.orderedDays());
    }

    @GetMapping
    public String list(Pageable pageable, Model model){



        return "room/operation-policy/list";
    }
}
