package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentAttemptFailService {

    private final PaymentAttemptRepository paymentAttemptRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String orderId, String code, String message) {
        PaymentAttempt attempt = paymentAttemptRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_ATTEMPT_NOT_FOUND));
        attempt.markFailed(code, message);
    }
}
