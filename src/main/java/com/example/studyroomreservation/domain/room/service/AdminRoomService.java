package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomImage;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.domain.room.repository.RoomRuleRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRoomService {

    private final RoomMapper roomMapper;
    private final RoomRepository roomRepository;
    private final RoomImageStorageService imageStorageService;

    private final OperationPolicyRepository operationPolicyRepository;
    private final RoomRuleRepository roomRuleRepository;
    private final RefundPolicyRepository refundPolicyRepository;

    @Transactional
    public Long createRoom(
            RoomCreateRequest request,
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        // 정책 조회 및 검증(존재 + 활성 상태)
        OperationPolicy operationPolicy = loadAndValidateOperationPolicy(request.operationPolicyId());
        RoomRule roomRule = loadAndValidateRoomRule(request.roomRuleId());
        validateRefundPolicy(request.refundPolicyId());

        // Room 엔티티 생성 및 저장
        Room newRoom = roomMapper.toEntity(request, operationPolicy, roomRule);
        roomRepository.saveAndFlush(newRoom); // GenerationType.IDENTITY라 id를 얻기 위해
        Long roomId = newRoom.getId();

        log.info("Room 엔티티 생성: id={}, name={}", roomId, request.name());

        // 이미지 저장 (파일시스템)
        try {
            saveRoomImages(newRoom, mainImage, generalImages);
            return roomId;
        } catch (BusinessException e) {
            cleanupOnFailure(roomId);
            throw e;
        } catch (Exception e) {
            cleanupOnFailure(roomId);
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "이미지 처리 중 오류: " + e.getMessage(), e);
        }
    }

    // ===== 이미지 저장 메서드 =====

    private void saveRoomImages(
            Room room,
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        long roomId = room.getId();

        imageStorageService.createRoomDirectory(roomId);

        // 메인 이미지
        String mainImagePath = imageStorageService.saveMainImage(roomId, mainImage);
        RoomImage.create(room, mainImagePath, RoomImage.ImageType.MAIN, 0);

        // 썸네일
        String thumbnailPath = imageStorageService.generateThumbnail(roomId, mainImagePath);
        RoomImage.create(room, thumbnailPath, RoomImage.ImageType.THUMBNAIL, 0);

        // 일반 이미지
        List<MultipartFile> validGeneralImages = filterNonEmptyImages(generalImages);
        List<String> generalPaths = imageStorageService.saveGeneralImages(roomId, validGeneralImages);

        for (int i = 0; i < generalPaths.size(); i++) {
            RoomImage.create(room, generalPaths.get(i), RoomImage.ImageType.GENERAL, i + 1);
        }

        log.info("Room 이미지 저장 성공: roomId={}, mainImage=1, thumbnail=1, generalImages={}",
                roomId, generalPaths.size());
    }

    private void cleanupOnFailure(Long roomId) {
        log.error("Room 이미지 등록 실패. 생성된 업로드 경로 삭제 : roomId={}", roomId);
        try {
            imageStorageService.deleteRoomDirectory(roomId);
        } catch (Exception cleanupEx) {
            log.warn("업로드 경로 삭제 실패 : roomId={}", roomId, cleanupEx);
        }
    }

    private List<MultipartFile> filterNonEmptyImages(List<MultipartFile> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();
    }

    // ===== 정책 검증 메서드 =====

    private OperationPolicy loadAndValidateOperationPolicy(Long id) {
        OperationPolicy policy = operationPolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.OP_POLICY_NOT_FOUND));

        if (!policy.isActive()) {
            throw new BusinessException(ErrorCode.OP_POLICY_INACTIVE,
                    "비활성화된 운영 정책: id=" + id);
        }
        return policy;
    }

    private RoomRule loadAndValidateRoomRule(Long id) {
        RoomRule rule = roomRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RR_NOT_FOUND));

        if (!rule.isActive()) {
            throw new BusinessException(ErrorCode.RR_INACTIVE,
                    "비활성화된 예약 규칙: id=" + id);
        }
        return rule;
    }

    private void validateRefundPolicy(Long id) {
        RefundPolicy policy = refundPolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.REF_POLICY_NOT_FOUND));

        if (!policy.isActive()) {
            throw new BusinessException(ErrorCode.REF_POLICY_INACTIVE,
                    "비활성화된 환불 정책: id=" + id);
        }
        // refundPolicyId는 Room에 id로 저장됨.
    }

    public Page<AdminRoomListResponse> getAdminRoomList(Pageable pageable) {
        return roomRepository.findAdminRoomList(pageable);
    }

    @Transactional
    public void toggleRoomStatus(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() == Room.RoomStatus.ACTIVE) {
            room.inactivate();
        } else {
            room.activate();
        }
    }

    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        room.softDelete();
    }


}
