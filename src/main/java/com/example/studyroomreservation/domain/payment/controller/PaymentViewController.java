package com.example.studyroomreservation.domain.payment.controller;

import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;

    //successUrl, failUrl 추가하기
    @GetMapping("/check")
    public String checkoutPage(
            @RequestParam Long reservationId,
            Model model
    ) {
        PaymentPrepareResponse response = paymentService.createPaymentAttempt(reservationId);

        model.addAttribute("payment", response);

        return "payment/check";
    }



}
