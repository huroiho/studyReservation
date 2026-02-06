package com.example.studyroomreservation.global;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String redirectToRoomList() {
        return "redirect:/rooms"; // "/rooms" 경로로 리디렉션
    }
}