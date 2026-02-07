package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.refund.service.RefundPolicyService;
import com.example.studyroomreservation.domain.room.dto.TempImageFiles;
import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.dto.request.RoomUpdateRequest;
import com.example.studyroomreservation.domain.room.dto.response.AdminRoomListResponse;
import com.example.studyroomreservation.domain.room.dto.response.RoomUpdateResponse;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
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

import java.util.ArrayList;
import java.util.List;

/**
 * - 전체 흐름 조정 (정책 검증 → 파일 I/O → DB 트랜잭션 → 파일 이동)
 * - 파일 저장 실패: DB 변경 없음
 * - DB 실패: 임시 파일 cleanup
 * - 파일 이동 실패: Room soft delete 보상 트랜잭션 + 임시 파일 cleanup
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminRoomService {

    private final RoomMapper roomMapper;
    private final RoomRepository roomRepository;
    private final OperationPolicyRepository operationPolicyRepository;
    private final RoomRuleRepository roomRuleRepository;

    private final AdminRoomTxService txService;
    private final RoomImageStorageService storageService;
    private final RefundPolicyService refundPolicyService;

    // ========== Room 생성 ==========

    /**
     * Room 생성
     * 1. 정책 검증
     * 2. 파일 임시 저장 (트랜잭션 밖)
     * 3. DB 저장 (단일 트랜잭션) - 최종 경로로 저장
     * 4. 파일 이동 (트랜잭션 밖)
     */
    public Long createRoom(
            RoomCreateRequest request,
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        // 1. 정책 검증
        OperationPolicy operationPolicy = loadAndValidateOperationPolicy(request.operationPolicyId());
        RoomRule roomRule = loadAndValidateRoomRule(request.roomRuleId());
        Long refundPolicyId = refundPolicyService.validateRefundPolicy(request.refundPolicyId());

        // 2. 파일 임시 저장
        List<MultipartFile> validGeneralImages = filterNonEmptyImages(generalImages);
        TempImageFiles imageFiles = storageService.saveImagesToTemp(mainImage, validGeneralImages);

        Long roomId = null;
        try {
            // 3. DB 저장 (최종 경로로 저장)
            roomId = txService.createRoomWithImages(
                    request, operationPolicy, roomRule, refundPolicyId, imageFiles
            );

            // 4. 파일 이동
            storageService.moveToRoomDirectory(imageFiles.tempDirId(), roomId);

            log.info("Room 생성 완료: id={}, name={}", roomId, request.name());
            return roomId;

        } catch (Exception e) {
            handleCreateFailure(imageFiles.tempDirId(), roomId, e);
            throw e;
        }
    }

    /**
     * 생성 실패 시 보상 처리
     */
    private void handleCreateFailure(String tempDirId, Long roomId, Exception originalError) {
        // 임시 파일 cleanup
        safeDeleteTempDirectory(tempDirId);

        // DB 저장 후 파일 이동 실패인 경우 -> Room soft delete
        if (roomId != null) {
            safeDeleteRoomDirectory(roomId);
            log.error("파일 이동 실패로 Room soft delete 수행: roomId={}", roomId, originalError);
            try {
                txService.softDeleteRoom(roomId);
            } catch (Exception e) {
                log.error("보상 트랜잭션(Room soft delete) 실패. 수동 확인 필요: roomId={}", roomId, e);
            }
        } else {
            log.error("Room 생성 실패, 임시 파일 cleanup 완료: tempDirId={}", tempDirId, originalError);
        }
    }

    // ========== Room 수정 ==========

    /**
     * Room 수정
     * 1. 정책 검증
     * 2. 새 파일 저장 (트랜잭션 밖)
     * 3. DB 업데이트 (단일 트랜잭션)
     * 4. old 파일 삭제
     */
    public void updateRoom(
            Long roomId,
            RoomUpdateRequest request,
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        // 1. 정책 검증
        OperationPolicy operationPolicy = findOperationPolicy(request.operationPolicyId());
        RoomRule roomRule = findRoomRule(request.roomRuleId());
        Long refundPolicyId = refundPolicyService.validateRefundPolicy(request.refundPolicyId());

        boolean hasNewMainImage = mainImage != null && !mainImage.isEmpty();
        List<MultipartFile> validGeneralImages = filterNonEmptyImages(generalImages);
        boolean hasNewGeneralImages = !validGeneralImages.isEmpty();

        // 이미지 변경 없으면 기본 정보만 업데이트
        if (!hasNewMainImage && !hasNewGeneralImages) {
            txService.updateRoomBasic(roomId, request, operationPolicy, roomRule, refundPolicyId);
            log.info("Room 수정 완료 (이미지 변경 없음): id={}", roomId);
            return;
        }

        // 2. 새 파일 저장
        List<String> newFilePaths = new ArrayList<>();
        String newMainPath = null;
        String newThumbPath = null;
        List<String> newGeneralPaths = List.of();

        try {
            if (hasNewMainImage) {
                newMainPath = storageService.saveMainImage(roomId, mainImage);
                newFilePaths.add(newMainPath);
                newThumbPath = storageService.generateThumbnail(roomId, newMainPath);
                newFilePaths.add(newThumbPath);
            }

            if (hasNewGeneralImages) {
                newGeneralPaths = storageService.saveGeneralImages(roomId, validGeneralImages);
                newFilePaths.addAll(newGeneralPaths);
            }

            // 3. DB 업데이트
            List<String> oldFilePaths = executeUpdate(
                    roomId, request, operationPolicy, roomRule, refundPolicyId,
                    hasNewMainImage, newMainPath, newThumbPath,
                    hasNewGeneralImages, newGeneralPaths
            );

            // 4. old 파일 삭제 (best-effort)
            storageService.deleteImageFiles(oldFilePaths);

            log.info("Room 수정 완료: id={}, mainImageReplaced={}, newGeneralImages={}",
                    roomId, hasNewMainImage, validGeneralImages.size());

        } catch (Exception e) {
            // 새 파일 cleanup
            log.error("Room 수정 실패, 새 파일 cleanup: roomId={}", roomId, e);
            storageService.deleteImageFiles(newFilePaths);
            throw e;
        }
    }

    private List<String> executeUpdate(
            Long roomId,
            RoomUpdateRequest request,
            OperationPolicy operationPolicy,
            RoomRule roomRule,
            Long refundPolicyId,
            boolean hasNewMainImage,
            String newMainPath,
            String newThumbPath,
            boolean hasNewGeneralImages,
            List<String> newGeneralPaths
    ) {
        if (hasNewMainImage && hasNewGeneralImages) {
            return txService.updateRoomWithAllImages(
                    roomId, request, operationPolicy, roomRule, refundPolicyId,
                    newMainPath, newThumbPath, newGeneralPaths
            );
        } else if (hasNewMainImage) {
            return txService.updateRoomWithMainImage(
                    roomId, request, operationPolicy, roomRule, refundPolicyId,
                    newMainPath, newThumbPath
            );
        } else {
            txService.updateRoomWithGeneralImages(
                    roomId, request, operationPolicy, roomRule, refundPolicyId,
                    newGeneralPaths
            );
            return List.of();
        }
    }

    // ========== Room 조회 ==========

    @Transactional(readOnly = true)
    public RoomUpdateResponse getRoomForEdit(Long roomId) {
        Room room = roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        String refundPolicyName = refundPolicyService.getRefundPolicyName(room.getRefundPolicyId());
        return roomMapper.toRoomUpdateResponse(room, refundPolicyName);
    }

    @Transactional(readOnly = true)
    public Page<AdminRoomListResponse> getAdminRoomList(Pageable pageable) {
        return roomRepository.findAdminRoomList(pageable);
    }

    // ========== Room 상태 변경 ==========

    public void toggleRoomStatus(Long roomId) {
        txService.toggleRoomStatus(roomId);
    }

    public void deleteRoom(Long roomId) {
        txService.softDeleteRoom(roomId);
    }

    // ========== 헬퍼 메서드 ==========

    private OperationPolicy loadAndValidateOperationPolicy(Long id) {
        OperationPolicy policy = findOperationPolicy(id);
        if (!policy.isActive()) {
            throw new BusinessException(ErrorCode.OP_POLICY_INACTIVE,
                    "비활성화된 운영 정책: id=" + id);
        }
        return policy;
    }

    private RoomRule loadAndValidateRoomRule(Long id) {
        RoomRule rule = findRoomRule(id);
        if (!rule.isActive()) {
            throw new BusinessException(ErrorCode.RR_INACTIVE,
                    "비활성화된 예약 규칙: id=" + id);
        }
        return rule;
    }

    private OperationPolicy findOperationPolicy(Long id) {
        return operationPolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.OP_POLICY_NOT_FOUND));
    }

    private RoomRule findRoomRule(Long id) {
        return roomRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.RR_NOT_FOUND));
    }

    private List<MultipartFile> filterNonEmptyImages(List<MultipartFile> images) {
        if (images == null) {
            return List.of();
        }
        return images.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();
    }

    private void safeDeleteTempDirectory(String tempDirId) {
        try {
            storageService.deleteTempDirectory(tempDirId);
        } catch (Exception e) {
            log.warn("임시 디렉토리 삭제 실패: tempDirId={}", tempDirId, e);
        }
    }

    private void safeDeleteRoomDirectory(Long roomId) {
        try {
            storageService.deleteRoomDirectory(roomId);
        } catch (Exception e) {
            log.warn("룸 디렉토리 삭제 실패: roomId={}", roomId, e);
        }
    }
}
