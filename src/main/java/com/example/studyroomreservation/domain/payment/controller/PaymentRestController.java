package com.example.studyroomreservation.domain.payment.controller;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentPrepareRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.service.PaymentService;
import com.example.studyroomreservation.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentRestController {

    private final PaymentService paymentService;


        @PostMapping("/prepare")
        public ResponseEntity<ApiResponse<PaymentPrepareResponse>> preparePayment(
                @RequestBody @Valid PaymentPrepareRequest request
        ) {
            PaymentPrepareResponse response = paymentService.createPaymentAttempt(request);
            return ResponseEntity.ok(ApiResponse.success(response));

        }
}


