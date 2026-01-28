package com.example.studyroomreservation.domain.payment.service;

import com.example.studyroomreservation.domain.member.entity.Member;
import com.example.studyroomreservation.domain.member.repository.MemberRepository;
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
import com.example.studyroomreservation.global.config.TossPaymentConfig;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentAttemptRepository paymentAttemptRepository;
    private final PaymentMapper paymentMapper;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final MemberRepository memberRepository;
    private final TossPaymentConfig tossPaymentConfig;

    private static final int RESERVATION_EXPIRE_EXTENSION_MINUTES = 3;

    @Transactional
    public PaymentPrepareResponse createPaymentAttempt(Long reservationId) {


        Reservation reservation = reservationRepository.findByIdWithLock(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));

        if (reservation.getStatus() != ReservationStatus.TEMP) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        boolean isFirstPaymentAttempt = !paymentAttemptRepository.existsByReservationId(reservationId);

        if (isFirstPaymentAttempt) {
            reservation.extendExpiresAt(RESERVATION_EXPIRE_EXTENSION_MINUTES);
        }

        int realAmount = reservation.getTotalAmount();
        PaymentAttempt paymentAttempt = PaymentAttempt.createPending(reservationId, realAmount, null);

        paymentAttemptRepository.save(paymentAttempt);

        Room room = roomRepository.findById(reservation.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        Member member = memberRepository.findById(reservation.getMemberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return paymentMapper.toPrepareResponse(
                paymentAttempt,
                room,
                member,
                tossPaymentConfig.getClientKey()
        );
    }
}