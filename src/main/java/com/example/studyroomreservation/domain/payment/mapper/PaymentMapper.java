package com.example.studyroomreservation.domain.payment.mapper;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.payment.dto.request.PaymentPrepareRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.dto.response.TossConfirmResponse;
import com.example.studyroomreservation.domain.payment.entity.Payment;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.entity.PaymentMethod;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.UUID;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PaymentMapper {


    default PaymentPrepareResponse toPrepareResponse(
            PaymentAttempt attempt,
            Room room,
            Member member,
            String clientKey
    ) {
        if (attempt == null) {
            return null;
        }
        return new PaymentPrepareResponse(
                clientKey,
                attempt.getOrderId(),
                room.getName(),
                attempt.getAmount(),
                member.getEmail(),
                member.getName(),
                member.getPhoneNumber()
        );
    }

    default Payment toPaymentSuccess(PaymentAttempt attempt, TossConfirmResponse confirm) {
        return Payment.createSuccess(
                attempt.getReservationId(),
                attempt.getOrderId(),
                attempt.getAmount(),
                attempt.getPaymentMethod(),
                confirm.paymentKey(),
                confirm.approvedAt().toLocalDateTime()
        );
    }
}
