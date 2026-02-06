package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomUpdateResponse;
import com.example.studyroomreservation.domain.room.entity.Room.AmenityType;
import com.example.studyroomreservation.domain.room.service.AdminRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomViewController {

    private final AdminRoomService adminRoomService;
    private final RefundPolicyService refundPolicyService;

    private static final String CREATE_VIEW = "room/admin/create";

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("amenityTypes", List.of(AmenityType.values()));
        model.addAttribute("mode", "create");
        return CREATE_VIEW;
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        RoomUpdateResponse room = adminRoomService.getRoomForEdit(id);

        model.addAttribute("room", room);
        model.addAttribute("roomId", id);
        model.addAttribute("amenityTypes", List.of(AmenityType.values()));
        model.addAttribute("mode", "edit");
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

    @PostMapping("/{id}/toggle")
    public String toggleRoomStatus(@PathVariable Long id) {
        adminRoomService.toggleRoomStatus(id);
        return "redirect:/admin/rooms";
    }

    @PostMapping("/{id}/delete")
    public String deleteRoom(@PathVariable Long id) {
        adminRoomService.deleteRoom(id);
        return "redirect:/admin/rooms";
    }
}
