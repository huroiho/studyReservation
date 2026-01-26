package com.example.studyroomreservation.domain.payment.mapper;

import com.example.studyroomreservation.domain.payment.dto.request.PaymentPrepareRequest;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)public interface PaymentMapper {

    /*@ObjectFactory
    default Payment creatPayment(){
        if(request == null) {
            return null;
        }
        return Payment.createPending(request.reservationId(), request.amount());
    }*/


    default PaymentAttempt createPaymentAttempt(PaymentPrepareRequest request) {
        if (request == null) {
            return null;
        }
        return PaymentAttempt.createPending(
                request.reservationId(),
                request.amount());
    }
}
