package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class UserRoomController {

    private static final int DEFAULT_PAGE_SIZE = 12;
    private static final int MAX_PAGE_SIZE = 48;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "price");

    private final RoomService roomService;

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(defaultValue = "name") String sort,
            Model model
    ) {
        // Sanitize inputs
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        String safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "name";
        Integer safeMinCapacity = (minCapacity != null && minCapacity >= 1) ? minCapacity : null;

        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, safeSort));
        Page<UserRoomListResponse> roomPage = roomService.getUserList(safeMinCapacity, pageable);

        model.addAttribute("page", roomPage);
        model.addAttribute("minCapacity", safeMinCapacity);
        model.addAttribute("sort", safeSort);

        return "room/user/list";
    }
}
