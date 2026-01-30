package com.example.studyroomreservation.domain.room.controller;
import com.example.studyroomreservation.domain.room.service.RoomRuleService;
import com.example.studyroomreservation.domain.room.validation.RoomRuleValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.example.studyroomreservation.domain.room.controller.RoomRuleControllerConstants.*;


@Controller
@RequiredArgsConstructor
@RequestMapping(BASE_PATH)
public class RoomRuleRestController {
    private final RoomRuleService roomRuleService;
    private final RoomRuleValidator roomRuleValidator;

    // 상태변경
    @PatchMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam boolean active) {
        roomRuleService.updateStatus(id, active);
        return ResponseEntity.ok().build();
    }
}
