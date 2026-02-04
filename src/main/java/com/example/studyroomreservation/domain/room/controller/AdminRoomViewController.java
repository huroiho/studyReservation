package com.example.studyroomreservation.domain.room.controller;

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

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/rooms")
public class AdminRoomViewController {

    private final AdminRoomService adminRoomService;

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
