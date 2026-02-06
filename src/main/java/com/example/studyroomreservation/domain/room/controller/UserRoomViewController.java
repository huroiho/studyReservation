package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.UserRoomDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.service.UserRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class UserRoomViewController {

    private static final int MAX_PAGE_SIZE = 48;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "price");

    private final UserRoomService userRoomService;

    // TODO : 정렬 기준 enum으로 분리하고 문자열로 들어간 정렬 기준 제거
    @GetMapping
    public String list(
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) List<String> amenityStrings,
            @RequestParam(defaultValue = "name") String sort,
            @PageableDefault(size = 12) Pageable pageable,
            Model model
    ) {
        int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        String safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "name";
        Integer safeMinCapacity = (minCapacity != null && minCapacity >= 1) ? minCapacity : null;

        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safeSize, Sort.by(Sort.Direction.ASC, safeSort));
        Page<UserRoomListResponse> roomPage = userRoomService.getUserList(safeMinCapacity, safeSort, amenityStrings, safePageable);

        model.addAttribute("page", roomPage);
        model.addAttribute("minCapacity", safeMinCapacity);
        model.addAttribute("sort", safeSort);
        model.addAttribute("amenityStrings", amenityStrings); //체크상태유지
        model.addAttribute("amenityTypes", Room.AmenityType.values());

        return "room/user/list";
    }

    @GetMapping("/{roomId}")
    public String detail(@PathVariable Long roomId, Model model) {
        UserRoomDetailResponse room = userRoomService.getUserDetail(roomId);
        model.addAttribute("room", room);

        return "room/user/detail";
    }
}
