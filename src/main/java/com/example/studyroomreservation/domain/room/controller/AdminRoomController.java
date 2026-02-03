package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.entity.Room.AmenityType;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import com.example.studyroomreservation.domain.room.repository.RoomRuleRepository;
import com.example.studyroomreservation.domain.room.service.AdminRoomService;
import com.example.studyroomreservation.domain.room.validation.RoomValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/rooms")
public class AdminRoomController {

    private static final String FORM = "form";
    private static final String CREATE_VIEW = "room/admin/room-form";
    private static final String REDIRECT_DETAIL = "redirect:/admin/rooms/";
    private static final String NEW_FORM_PATH = "/new";

    private final AdminRoomService roomService;
    private final RoomValidator roomValidator;
    private final OperationPolicyRepository operationPolicyRepository;
    private final RoomRuleRepository roomRuleRepository;
    private final RefundPolicyRepository refundPolicyRepository;

    @InitBinder(FORM)
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(roomValidator);
    }

    @GetMapping(NEW_FORM_PATH)
    public String createForm(Model model) {
        model.addAttribute(FORM, new RoomCreateRequest(
                null,null,null,null,null,null,Set.of()
        ));
        injectFormData(model);
        return CREATE_VIEW;
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute(FORM) RoomCreateRequest form,
            BindingResult bindingResult,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam(value = "generalImages", required = false) List<MultipartFile> generalImages,
            Model model
    ) {
        // Bean Validation 에러가 없을 때만 이미지 검증 수행
        if (!bindingResult.hasErrors())
                roomValidator.validateImages(mainImage, generalImages, bindingResult);

        if (bindingResult.hasErrors()) {
            log.debug("Validation errors: {}", bindingResult.getAllErrors());
            injectFormData(model);
            return CREATE_VIEW;
        }

        Long roomId = roomService.createRoom(form, mainImage, generalImages);
        log.info("Room created successfully: id={}, name={}", roomId, form.name());

        return REDIRECT_DETAIL + roomId;
    }

    // TODO : 컨트롤러에서 레포 조회하지 않음. 서비스로 수정하던가 다른 방향으로.
    private void injectFormData(Model model) {
        // 활성화된 정책 목록 조회
        model.addAttribute("operationPolicies",
                operationPolicyRepository.findAllByIsActiveTrue());
        model.addAttribute("roomRules",
                roomRuleRepository.findAllByIsActiveTrue());
        model.addAttribute("refundPolicies",
                refundPolicyRepository.findAllByIsActiveTrue());

        // Amenity 옵션
        model.addAttribute("amenityTypes", Arrays.asList(AmenityType.values()));
    }
}
