package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.service.OperationPolicyService;
import com.example.studyroomreservation.domain.room.validation.OperationPolicyValidator;
import com.example.studyroomreservation.domain.room.web.OperationPolicyFormFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import static com.example.studyroomreservation.domain.room.controller.OperationPolicyControllerConstants.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(BASE_PATH)
public class OperationPolicyController {

    private final OperationPolicyService operationPolicyService;
    private final OperationPolicyFormFactory formFactory;
    private final OperationPolicyValidator operationPolicyValidator;

    /**
     * WebDataBinder에 커스텀 Validator 등록
     * - @Valid 검증 후 추가로 비즈니스 검증을 수행
     * - 모든 검증 에러가 BindingResult에 자동으로 추가됨
     */
    @InitBinder(FORM)
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(operationPolicyValidator);
    }

    @GetMapping(NEW_FORM_PATH)
    public String createForm(Model model) {
        model.addAttribute(FORM, formFactory.emptyCreateForm());
        injectCommonModel(model);
        return CREATE_VIEW;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute(FORM) OperationPolicyCreateRequest form,
            BindingResult br,
            Model model
    ) {

        // @Valid 검증 + Validator 검증이 모두 완료되어 br에 담김
        if (br.hasErrors()) {
            log.debug("Validation errors: {}", br.getAllErrors());
            injectCommonModel(model);
            return CREATE_VIEW;
        }

        Long id = operationPolicyService.create(form);
        log.info("Operation policy created successfully: id={}, name={}", id, form.name());

        return REDIRECT_BASE + id;
    }

    private void injectCommonModel(Model model) {
        model.addAttribute(SLOT_UNITS, formFactory.slotUnits());
        model.addAttribute(HOURS, formFactory.hourOptions());
        model.addAttribute(DAYS, formFactory.orderedDays());
    }

    @GetMapping
    public String list(
            @RequestParam(name = PARAM_SEARCH, required = false) String search,
            @RequestParam(name = PARAM_STATUS, required = false) String status,
            Pageable pageable,
            Model model
    ) {
        Boolean isActive = parseStatusFilter(status);
        String keyword = (search != null && !search.isBlank()) ? search.trim() : null;

        model.addAttribute(PAGE, operationPolicyService.getList(keyword, isActive, pageable));

        return LIST_VIEW;
    }

    private Boolean parseStatusFilter(String status) {
        if (STATUS_ACTIVE.equalsIgnoreCase(status)) {
            return true;
        }
        if (STATUS_INACTIVE.equalsIgnoreCase(status)) {
            return false;
        }
        return null; // ALL
    }

    @GetMapping(DETAIL_PATH)
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute(POLICY, operationPolicyService.getDetail(id));
        return DETAIL_VIEW;
    }

    @PostMapping(ACTIVATE_PATH)
    public String activate(
            @PathVariable Long id,
            @RequestParam(name = PARAM_REDIRECT, required = false) String redirect
    ) {
        operationPolicyService.activate(id);
        return resolveRedirect(redirect, id);
    }

    @PostMapping(DEACTIVATE_PATH)
    public String deactivate(
            @PathVariable Long id,
            @RequestParam(name = PARAM_REDIRECT, required = false) String redirect
    ) {
        operationPolicyService.deactivate(id);
        return resolveRedirect(redirect, id);
    }

    private String resolveRedirect(String redirect, Long id) {
        if (REDIRECT_TARGET_LIST.equals(redirect)) {
            return REDIRECT_LIST;
        }
        return REDIRECT_BASE + id;
    }

    @PostMapping(DELETE_PATH)
    public String delete(@PathVariable Long id) {
        operationPolicyService.delete(id);
        return REDIRECT_LIST;
    }
}
