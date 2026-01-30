package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.reservation.repository.ReservationRepository;
import com.example.studyroomreservation.domain.room.dto.request.OperationPolicyCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.OperationPolicyDetailResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.mapper.OperationPolicyMapper;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OperationPolicyService {

    private final OperationPolicyRepository operationPolicyRepository;
    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final OperationPolicyMapper operationPolicyMapper;

    // 운영 정책 생성 - 검증은 OperationPolicyValidator에서 이미 완료됨
    @Transactional
    public Long create(OperationPolicyCreateRequest request){
        OperationPolicy newPolicy = operationPolicyMapper.createPolicy(request);
        return operationPolicyRepository.save(newPolicy).getId();
    }

    // 운영 정책 상세 조회 - 팩트 데이터만 조회, 메시지 조합은 View 책임
    public OperationPolicyDetailResponse getDetail(Long id) {
        OperationPolicy policy = operationPolicyRepository.findByIdWithSchedules(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.OP_POLICY_NOT_FOUND));

        List<Room> connectedRooms = roomRepository.findByOperationPolicyId(id);
        boolean hasReservationReference = reservationRepository.existsByAppliedOperationPolicyId(id);

        return operationPolicyMapper.toDetailResponse(policy, connectedRooms, hasReservationReference);
    }

    /**
     * 정책 상태 변경 및 삭제는 추후 다른 policy에서도 동일하게 작성될 가능성이 높음
     * 추후 공통 Policy 제약 검증 컴포넌트로 분리 예정
     */

    // 운영 정책 활성화 (이미 활성 상태면 no-op)
    @Transactional
    public void activate(Long id) {
        OperationPolicy policy = findPolicyById(id);
        policy.activate();
    }

    // 운영 정책 비활성화 (연결된 룸이 있으면 불가, 이미 비활성 상태면 no-op)
    @Transactional
    public void deactivate(Long id) {
        OperationPolicy policy = findPolicyById(id);
        assertNotUsedByRooms(id);
        policy.deactivate();
    }

    // 운영 정책 삭제 - 연결된 룸이 있거나 예약 이력이 있으면 삭제 불가
    @Transactional
    public void delete(Long id) {
        OperationPolicy policy = findPolicyById(id);
        assertNotUsedByRooms(id);
        assertNotUsedByReservations(id);
        operationPolicyRepository.delete(policy);
    }

    // === 공통 메서드 ===
    private OperationPolicy findPolicyById(Long id) {
        return operationPolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.OP_POLICY_NOT_FOUND));
    }

    private void assertNotUsedByRooms(Long policyId) {
        if (roomRepository.existsByOperationPolicyId(policyId)) {
            throw new BusinessException(ErrorCode.OP_POLICY_IN_USE_BY_ROOM);
        }
    }

    private void assertNotUsedByReservations(Long policyId) {
        if (reservationRepository.existsByAppliedOperationPolicyId(policyId)) {
            throw new BusinessException(ErrorCode.OP_POLICY_IN_USE_BY_RESERVATION);
        }
    }
}
