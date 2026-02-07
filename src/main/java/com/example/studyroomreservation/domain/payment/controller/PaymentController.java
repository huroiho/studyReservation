package com.example.studyroomreservation.domain.payment.controller;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
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

import static com.example.studyroomreservation.domain.payment.controller.PaymentConstants.*;

@Controller
@RequestMapping(VIEW_PAYMENT_BASE)
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    //successUrl, failUrl 추가하기
    @GetMapping(VIEW_PAYMENT_CHECK)
    public String checkoutPage(
            @RequestParam Long reservationId,
            Model model
    ) {
        PaymentPrepareResponse response = paymentService.createPaymentAttempt(reservationId);

        model.addAttribute("payment", response);

        return TMPL_PAYMENT_CHECK;
    }

    @GetMapping(VIEW_PAYMENT_APPROVE)
    public String approveSuccess(
            @RequestParam(value = "paymentType", required = false) String paymentType,
            @Valid @ModelAttribute PaymentApproveRequest request,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()){
            return TMPL_PAYMENT_FAIL;
        }
        Long reservationId = paymentService.approveSuccess(paymentType, request);
        return redirectReservationDetail(reservationId);
    }

    @GetMapping(VIEW_PAYMENT_FAIL)
    public String fail(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message
    ) {
        Long reservationId = paymentService.handlePaymentFail(orderId, code, message);

        if (reservationId != null) {
            return redirectReservationDetail(reservationId);
        }
        return REDIRECT_RESERVATION_LIST;
    }
}
