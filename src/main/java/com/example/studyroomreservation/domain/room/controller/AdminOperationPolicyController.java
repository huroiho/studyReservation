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

import static com.example.studyroomreservation.domain.room.controller.RoomConstants.*;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(VIEW_ADMIN_OP_POLICY_BASE)
public class AdminOperationPolicyController {

    private final OperationPolicyService operationPolicyService;
    private final OperationPolicyFormFactory formFactory;
    private final OperationPolicyValidator operationPolicyValidator;

    /**
     * WebDataBinder에 커스텀 Validator 등록
     * - @Valid 검증 후 추가로 비즈니스 검증을 수행
     * - 모든 검증 에러가 BindingResult에 자동으로 추가됨
     */
    @InitBinder(MODEL_OP_FORM)
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(operationPolicyValidator);
    }

    @GetMapping(VIEW_ADMIN_OP_POLICY_NEW)
    public String createForm(Model model) {
        model.addAttribute(MODEL_OP_FORM, formFactory.emptyCreateForm());
        injectCommonModel(model);
        return TMPL_ADMIN_OP_POLICY_CREATE;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute(MODEL_OP_FORM) OperationPolicyCreateRequest form,
            BindingResult br,
            Model model
    ) {

        // @Valid 검증 + Validator 검증이 모두 완료되어 br에 담김
        if (br.hasErrors()) {
            log.debug("Validation errors: {}", br.getAllErrors());
            injectCommonModel(model);
            return TMPL_ADMIN_OP_POLICY_CREATE;
        }

        Long id = operationPolicyService.create(form);
        log.info("Operation policy created successfully: id={}, name={}", id, form.name());

        return REDIRECT_ADMIN_OP_POLICY_DETAIL + id;
    }

    private void injectCommonModel(Model model) {
        model.addAttribute(MODEL_OP_SLOT_UNITS, formFactory.slotUnits());
        model.addAttribute(MODEL_OP_HOURS, formFactory.hourOptions());
        model.addAttribute(MODEL_OP_DAYS, formFactory.orderedDays());
    }

    @GetMapping
    public String list(
            @RequestParam(name = PARAM_OP_SEARCH, required = false) String search,
            @RequestParam(name = PARAM_OP_STATUS, required = false) String status,
            Pageable pageable,
            Model model
    ) {
        Boolean isActive = parseStatusFilter(status);
        String keyword = (search != null && !search.isBlank()) ? search.trim() : null;

        model.addAttribute(MODEL_OP_PAGE, operationPolicyService.getList(keyword, isActive, pageable));

        return TMPL_ADMIN_OP_POLICY_LIST;
    }

    private Boolean parseStatusFilter(String status) {
        if (VAL_OP_STATUS_ACTIVE.equalsIgnoreCase(status)) {
            return true;
        }
        if (VAL_OP_STATUS_INACTIVE.equalsIgnoreCase(status)) {
            return false;
        }
        return null; // ALL
    }

    @GetMapping(VIEW_ADMIN_OP_POLICY_DETAIL)
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute(MODEL_OP_POLICY, operationPolicyService.getDetail(id));
        return TMPL_ADMIN_OP_POLICY_DETAIL;
    }

    @PostMapping(VIEW_ADMIN_OP_POLICY_ACTIVATE)
    public String activate(
            @PathVariable Long id,
            @RequestParam(name = PARAM_OP_REDIRECT, required = false) String redirect
    ) {
        operationPolicyService.activate(id);
        return resolveRedirect(redirect, id);
    }

    @PostMapping(VIEW_ADMIN_OP_POLICY_DEACTIVATE)
    public String deactivate(
            @PathVariable Long id,
            @RequestParam(name = PARAM_OP_REDIRECT, required = false) String redirect
    ) {
        operationPolicyService.deactivate(id);
        return resolveRedirect(redirect, id);
    }

    private String resolveRedirect(String redirect, Long id) {
        if (VAL_OP_REDIRECT_LIST.equals(redirect)) {
            return REDIRECT_ADMIN_OP_POLICY_LIST;
        }
        return REDIRECT_ADMIN_OP_POLICY_DETAIL + id;
    }

    @PostMapping(VIEW_ADMIN_OP_POLICY_DELETE)
    public String delete(@PathVariable Long id) {
        operationPolicyService.delete(id);
        return REDIRECT_ADMIN_OP_POLICY_LIST;
    }
}
