package com.example.studyroomreservation.domain.payment.controller;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.service.PaymentAttemptFailService;
import com.example.studyroomreservation.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static com.example.studyroomreservation.domain.payment.controller.PaymentControllerConstants.*;

@Controller
@RequestMapping(BASE)
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentService paymentService;
    private final PaymentAttemptFailService paymentAttemptFailService;


    //successUrl, failUrl 추가하기
    @GetMapping(CHECK)
    public String checkoutPage(
            @RequestParam Long reservationId,
            Model model
    ) {
        PaymentPrepareResponse response = paymentService.createPaymentAttempt(reservationId);

        model.addAttribute("payment", response);

        return PAYMENT_CHECK;
    }

    @GetMapping(APPROVE)
    public String approveSuccess(
            @RequestParam(value = "paymentType", required = false) String paymentType,
            @Valid @ModelAttribute PaymentApproveRequest request,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()){
            return PAYMENT_FAIL;
        }
        paymentService.approveSuccess(paymentType,request);
        return REDIRECT_RESERVATIONS; // TODO : 예약 파트 하면 주소 변경 필요
    }

    @GetMapping(FAIL)
    public String fail(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message
    ) {
        if (orderId != null) {
            paymentAttemptFailService.markFailed(orderId, code, message);
        }
        return REDIRECT_RESERVATIONS;
    }
}
