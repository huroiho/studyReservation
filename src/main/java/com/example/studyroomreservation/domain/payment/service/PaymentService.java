package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
import com.example.studyroomreservation.domain.payment.dto.request.PaymentPrepareRequest;
import com.example.studyroomreservation.domain.payment.dto.response.PaymentPrepareResponse;
import com.example.studyroomreservation.domain.payment.entity.PaymentAttempt;
import com.example.studyroomreservation.domain.payment.mapper.PaymentMapper;
import com.example.studyroomreservation.domain.payment.repository.PaymentAttemptRepository;
import com.example.studyroomreservation.domain.payment.repository.PaymentRepository;
import com.example.studyroomreservation.domain.reservation.entity.Reservation;
import com.example.studyroomreservation.domain.reservation.entity.ReservationStatus;
import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentMapper paymentMapper;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;

    // 결제 시도 생성
    @Transactional
    public PaymentPrepareResponse createPaymentAttempt(PaymentPrepareRequest request) {
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        Member member = memberRepository.findById(reservation.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.TEMP){
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        if (reservation.getTotalAmount() != request.amount()){
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 만료 시간 추가는 1회만(동일한 결제 이력있는지로 판단)
        boolean isFirstPaymentAttempt = !paymentAttemptRepository.existsByReservationId(request.reservationId());
        if(isFirstPaymentAttempt){

            // 만료 시간 3분 추가
            reservation.extendExpiresAt(LocalDateTime.now(), 3);
        }

        PaymentAttempt paymentAttempt = paymentMapper.createPaymentAttempt(request);
        paymentAttemptRepository.save(paymentAttempt);

        return paymentMapper.toPrepareResponse(
                paymentAttempt,
                room,
                member
        );
    }
}
