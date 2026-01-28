package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.service.OperationPolicyService;
import com.example.studyroomreservation.domain.room.validation.validator.OperationPolicyValidator;
import com.example.studyroomreservation.domain.room.web.OperationPolicyFormFactory;
import com.example.studyroomreservation.global.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/operation-policies")
public class OperationPolicyController {

    private final OperationPolicyService operationPolicyService;
    private final OperationPolicyFormFactory formFactory;
    private final OperationPolicyValidator operationPolicyValidator;

    /**
     * WebDataBinder에 커스텀 Validator 등록
     * - @Valid 검증 후 추가로 비즈니스 검증을 수행
     * - 모든 검증 에러가 BindingResult에 자동으로 추가됨
     */
    @InitBinder("form")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(operationPolicyValidator);
    }

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

        // @Valid 검증 + Validator 검증이 모두 완료되어 br에 담김
        if (br.hasErrors()) {
            log.debug("Validation errors: {}", br.getAllErrors());
            injectCommonModel(model);
            return "room/operation-policy/create";
        }

        Long id = operationPolicyService.create(form);
        log.info("Operation policy created successfully: id={}, name={}", id, form.name());

        return "redirect:/admin/operation-policies/" + id;
    }

    private void injectCommonModel(Model model) {
        model.addAttribute("slotUnits", formFactory.slotUnits());
        model.addAttribute("hours", formFactory.hourOptions());
        model.addAttribute("days", formFactory.orderedDays());
    }
}
