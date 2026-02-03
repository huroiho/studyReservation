package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.service.AdminRoomService;
import com.example.studyroomreservation.domain.room.validation.RoomImageInputValidator;
import com.example.studyroomreservation.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Admin Room REST API 컨트롤러
 * - Room 생성 POST (multipart/form-data)
 * - 에러 발생 시 JSON 응답 (ApiExceptionHandler에서 처리)
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomRestController {

    private final AdminRoomService adminRoomService;
    private final RoomImageInputValidator imageInputValidator;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Long>> createRoom(
            @Valid @ModelAttribute RoomCreateRequest request,
            @RequestParam("mainImage") MultipartFile mainImage,
            @RequestParam(value = "generalImages", required = false) List<MultipartFile> generalImages
    ) {
        imageInputValidator.validate(mainImage, generalImages);

        Long roomId = adminRoomService.createRoom(request, mainImage, generalImages);

        log.info("Room 등록 성공 REST API: id={}, name={}", roomId, request.name());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(roomId));
    }
}
