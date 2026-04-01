package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.dto.response.RoomDetailResponse;
import com.example.studyroomreservation.domain.room.dto.response.UserRoomListResponse;
import com.example.studyroomreservation.domain.room.service.UserRoomQueryService;
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

import java.util.Set;

import static com.example.studyroomreservation.domain.room.controller.RoomConstants.*;

@Controller
@RequestMapping(VIEW_ROOM_BASE)
@RequiredArgsConstructor
public class RoomController {

    private static final int MAX_PAGE_SIZE = 48;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("name", "price");

    private final UserRoomQueryService userRoomQueryService;

    @GetMapping
    public String list(
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(defaultValue = "name") String sort,
            @PageableDefault(size = 12) Pageable pageable,
            Model model
    ) {
        int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);

        String safeSort = ALLOWED_SORT_FIELDS.contains(sort) ? sort : "name";
        Integer safeMinCapacity = (minCapacity != null && minCapacity >= 1) ? minCapacity : null;

        Sort sortSpec = Sort.by(Sort.Direction.ASC, safeSort)
                .and(Sort.by(Sort.Direction.ASC, "id"));

        Pageable safePageable = PageRequest.of(pageable.getPageNumber(), safeSize, sortSpec);
        Page<UserRoomListResponse> roomPage = userRoomQueryService.getUserList(safeMinCapacity, safePageable);

        model.addAttribute("page", roomPage);
        model.addAttribute("minCapacity", safeMinCapacity);
        model.addAttribute("sort", safeSort);

        return TMPL_ROOM_LIST;
    }

    @GetMapping(VIEW_ROOM_DETAIL)
    public String detail(@PathVariable Long roomId, Model model) {
        RoomDetailResponse room = userRoomQueryService.getUserDetail(roomId);
        model.addAttribute("room", room);
        model.addAttribute("backUrl", VIEW_ROOM_BASE);

        return TMPL_ROOM_DETAIL;
    }
}
