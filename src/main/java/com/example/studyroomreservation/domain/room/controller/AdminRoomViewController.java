package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.Room.AmenityType;
import com.example.studyroomreservation.domain.room.service.AdminRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.service.AdminRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomViewController {

    private final AdminRoomService adminRoomService;
    private static final String CREATE_VIEW = "room/admin/create";

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("amenityTypes", List.of(AmenityType.values()));
        return CREATE_VIEW;
    }

    @GetMapping
    public String adminRoomList(
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        Page<AdminRoomListResponse> page =
                adminRoomService.getAdminRoomList(pageable);

        model.addAttribute("rooms", page.getContent());
        model.addAttribute("page", page);

        return "room/admin/room-list";
    }
}
