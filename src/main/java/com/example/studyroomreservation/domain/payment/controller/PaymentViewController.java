package com.example.studyroomreservation.domain.payment.controller;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.service.PaymentAttemptFailService;
import com.example.studyroomreservation.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;
    private final PaymentAttemptFailService paymentAttemptFailService;


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

    @GetMapping("/payments/fail")
    public String fail(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message
    ) {
        if (orderId != null) {
            paymentAttemptFailService.markFailed(orderId, code, message);
        }
        return "redirect:/reservations";
    }
}
