package com.example.studyroomreservation.domain.room.controller;

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

import static com.example.studyroomreservation.domain.room.controller.RoomConstants.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(VIEW_ADMIN_ROOM_BASE)
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomController {

    private final AdminRoomService adminRoomService;

    @GetMapping(VIEW_ADMIN_ROOM_CREATE)
    public String showCreateForm(Model model) {
        model.addAttribute("amenityTypes", List.of(AmenityType.values()));
        model.addAttribute("mode", "create");
        return TMPL_ADMIN_ROOM_CREATE;
    }

    @GetMapping(VIEW_ADMIN_ROOM_EDIT)
    public String showEditForm(@PathVariable Long id, Model model) {
        RoomUpdateResponse room = adminRoomService.getRoomForEdit(id);

        model.addAttribute("room", room);
        model.addAttribute("roomId", id);
        model.addAttribute("amenityTypes", List.of(AmenityType.values()));
        model.addAttribute("mode", "edit");
        return TMPL_ADMIN_ROOM_CREATE;
    }

    @GetMapping
    public String adminRoomList(
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {

        Page<AdminRoomListResponse> page =
                adminRoomService.getAdminRoomList(pageable);

        model.addAttribute("rooms", page.getContent());
        model.addAttribute("page", page);

        return TMPL_ADMIN_ROOM_LIST;
    }

    @PostMapping(VIEW_ADMIN_ROOM_TOGGLE)
    public String toggleRoomStatus(@PathVariable Long id) {
        adminRoomService.toggleRoomStatus(id);
        return REDIRECT_ADMIN_ROOM_LIST;
    }

    @PostMapping(VIEW_ADMIN_ROOM_DELETE)
    public String deleteRoom(@PathVariable Long id) {
        adminRoomService.deleteRoom(id);
        return REDIRECT_ADMIN_ROOM_LIST;
    }
}
