package com.example.studyroomreservation.domain.payment.controller;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/payments/approve")
    public String approveSuccess(
            @RequestParam(value = "paymentType", required = false) String paymentType,
            @Valid @ModelAttribute PaymentApproveRequest request,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()){
            return "payment/fail";
        }
        paymentService.approveSuccess(paymentType,request);
        return "redirect:/reservations"; // TODO : 예약 파트 하면 주소 변경 필요
    }
}
