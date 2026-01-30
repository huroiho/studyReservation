package com.example.studyroomreservation.domain.payment.mapper;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentApproveRequest;
import com.example.studyroomreservation.domain.payment.dto.request.TossConfirmRequest;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TossConfirmMapper {

    @Mapping(target = "orderId", source = "approve.orderId")
    @Mapping(target = "paymentKey", source = "approve.paymentKey")
    @Mapping(target = "amount", source = "attempt.amount") // 서버 기준 금액
    TossConfirmRequest toConfirmRequest(PaymentApproveRequest approve, PaymentAttempt attempt);
}
