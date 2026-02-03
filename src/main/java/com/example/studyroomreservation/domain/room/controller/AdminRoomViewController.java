package com.example.studyroomreservation.domain.room.controller;

import com.example.studyroomreservation.domain.room.entity.Room.AmenityType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/rooms")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRoomViewController {

    private static final String CREATE_VIEW = "room/admin/create";

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("amenityTypes", List.of(AmenityType.values()));
        return CREATE_VIEW;
    }
}
