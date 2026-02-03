package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
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

    @Transactional
    public Long createRoom(
            RoomCreateRequest request,
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        // 정책 엔티티 조회 (Validator에서 존재/활성 검증 완료)
        OperationPolicy operationPolicy = operationPolicyRepository.findById(request.operationPolicyId())
                .orElseThrow(() -> new BusinessException(ErrorCode.OP_POLICY_NOT_FOUND));

        RoomRule roomRule = roomRuleRepository.findById(request.roomRuleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_RULE_NOT_FOUND));

        Room newRoom = roomMapper.toEntity(request, operationPolicy, roomRule);

        roomRepository.saveAndFlush(newRoom);
        Long roomId = newRoom.getId();

        log.info("Created room entity: id={}, name={}", roomId, request.name());

        // 이미지 저장 (파일 시스템)
        try {
            // 디렉토리 생성
            imageStorageService.createRoomDirectory(roomId);

            // 메인 이미지 저장
            String mainImagePath = imageStorageService.saveMainImage(roomId, mainImage);
            RoomImage.create(newRoom, mainImagePath, RoomImage.ImageType.MAIN, 0);

            // 썸네일 생성
            String thumbnailPath = imageStorageService.generateThumbnail(roomId, mainImagePath);
            RoomImage.create(newRoom, thumbnailPath, RoomImage.ImageType.THUMBNAIL, 0);

            // 일반 이미지 저장
            List<MultipartFile> validGeneralImages = filterNonEmptyImages(generalImages);
            List<String> generalPaths = imageStorageService.saveGeneralImages(roomId, validGeneralImages);
            for (int i = 0; i < generalPaths.size(); i++) {
                RoomImage.create(newRoom, generalPaths.get(i), RoomImage.ImageType.GENERAL, i + 1);
            }

            log.info("Room images saved: roomId={}, mainImage=1, thumbnail=1, generalImages={}",
                    roomId, generalPaths.size());
            return roomId;

        } catch (Exception e) {
            // 파일 정리 (best-effort)
            log.error("Failed to save room images, attempting cleanup: roomId={}", roomId, e);
            try {
                imageStorageService.deleteRoomDirectory(roomId);
            } catch (Exception cleanupEx) {
                log.warn("Cleanup failed for roomId={}", roomId, cleanupEx);
            }
            throw e; // 원본 예외 재던지기 → 트랜잭션 롤백
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
}
