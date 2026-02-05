package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.refund.entity.RefundPolicy;
import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.dto.request.RoomUpdateRequest;
import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomUpdateResponse;
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
    private final OperationPolicyRepository operationPolicyRepository;
    private final RoomRuleRepository roomRuleRepository;

    private final RoomImageStorageService imageStorageService;
    private final RefundPolicyService refundPolicyService;

    @Transactional
    public Long createRoom(
            RoomCreateRequest request,
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        // 정책 조회 및 검증(존재 + 활성 상태)
        OperationPolicy operationPolicy = loadAndValidateOperationPolicy(request.operationPolicyId());
        RoomRule roomRule = loadAndValidateRoomRule(request.roomRuleId());
        Long refundPolicyId  = refundPolicyService.validateRefundPolicy(request.refundPolicyId());

        // Room 엔티티 생성 및 저장
        Room newRoom = roomMapper.toEntity(request, operationPolicy, roomRule, refundPolicyId);
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


    // ===== Room 수정 =====

    @Transactional
    public void updateRoom(
            Long roomId,
            RoomUpdateRequest request,
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        // 1. 기존 Room 조회 (deletedAt IS NULL, images/operationPolicy/roomRule 함께 fetch)
        Room room = roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        // 2. 정책 조회 및 검증
        OperationPolicy operationPolicy = loadAndValidateOperationPolicy(request.operationPolicyId());
        RoomRule roomRule = loadAndValidateRoomRule(request.roomRuleId());
        Long refundPolicyId = refundPolicyService.validateRefundPolicy(request.refundPolicyId());

        // 3. 기본 필드 + 정책 업데이트 (dirty checking)
        room.updateRoom(
                request.name(), request.price(), request.maxCapacity(),
                operationPolicy, roomRule, refundPolicyId, request.amenities()
        );

        // 4. 메인 이미지 교체 (제공된 경우에만)
        boolean hasNewMainImage = mainImage != null && !mainImage.isEmpty();
        if (hasNewMainImage) {
            replaceMainImages(room, mainImage);
        }

        // 5. 일반 이미지 추가 (기존 GENERAL은 유지)
        List<MultipartFile> validGeneralImages = filterNonEmptyImages(generalImages);
        if (!validGeneralImages.isEmpty()) {
            appendGeneralImages(room, validGeneralImages);
        }

        log.info("Room 수정 완료: id={}, name={}, mainImageReplaced={}, newGeneralImages={}",
                roomId, request.name(), hasNewMainImage, validGeneralImages.size());
    }

    private void replaceMainImages(Room room, MultipartFile newMainImage) {
        Long roomId = room.getId();

        // 기존 MAIN/THUMBNAIL 파일 삭제
        room.getImages().stream()
                .filter(img -> img.getType() == RoomImage.ImageType.MAIN
                        || img.getType() == RoomImage.ImageType.THUMBNAIL)
                .map(RoomImage::getImageUrl)
                .forEach(imageStorageService::deleteImageFile);

        // 기존 MAIN/THUMBNAIL 엔티티 제거 (orphanRemoval → DB 삭제)
        room.removeImagesByType(RoomImage.ImageType.MAIN);
        room.removeImagesByType(RoomImage.ImageType.THUMBNAIL);

        // 새 메인 이미지 저장
        String mainImagePath = imageStorageService.saveMainImage(roomId, newMainImage);
        RoomImage.create(room, mainImagePath, RoomImage.ImageType.MAIN, 0);

        // 새 썸네일 생성
        String thumbnailPath = imageStorageService.generateThumbnail(roomId, mainImagePath);
        RoomImage.create(room, thumbnailPath, RoomImage.ImageType.THUMBNAIL, 0);
    }

    private void appendGeneralImages(Room room, List<MultipartFile> newGeneralImages) {
        Long roomId = room.getId();

        // 기존 GENERAL 이미지의 최대 sortOrder
        int maxSortOrder = room.getImages().stream()
                .filter(img -> img.getType() == RoomImage.ImageType.GENERAL)
                .mapToInt(RoomImage::getSortOrder)
                .max()
                .orElse(0);

        List<String> paths = imageStorageService.saveGeneralImages(roomId, newGeneralImages);
        for (int i = 0; i < paths.size(); i++) {
            RoomImage.create(room, paths.get(i), RoomImage.ImageType.GENERAL, maxSortOrder + i + 1);
        }
    }

    public RoomUpdateResponse getRoomForEdit(Long roomId) {
        Room room = roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        String refundPolicyName = refundPolicyService.getRefundPolicyName(room.getRefundPolicyId());

        return roomMapper.toRoomUpdateResponse(room, refundPolicyName);
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
