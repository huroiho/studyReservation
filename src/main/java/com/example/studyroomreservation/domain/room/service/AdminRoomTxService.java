package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.TempImageFiles;
import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.dto.request.RoomUpdateRequest;
import com.example.studyroomreservation.domain.room.entity.OperationPolicy;
import com.example.studyroomreservation.domain.room.entity.Room;
import com.example.studyroomreservation.domain.room.entity.RoomImage;
import com.example.studyroomreservation.domain.room.entity.RoomRule;
import com.example.studyroomreservation.domain.room.mapper.RoomMapper;
import com.example.studyroomreservation.domain.room.repository.RoomRepository;
import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Room DB 트랜잭션 서비스
 * <p>
 * 책임:
 * - Room/RoomImage 엔티티 CRUD
 * - 파일명 기반 최종 경로 조립 후 DB 저장
 * <p>
 * 이 서비스는 파일 I/O를 절대 수행하지 않습니다.
 */
@Service
@RequiredArgsConstructor
public class AdminRoomTxService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    private static final int MAX_GENERAL_IMAGES = 10;

    // ========== Room 생성 ==========

    /**
     * Room + RoomImage 생성 (단일 트랜잭션)
     * DB에 저장되는 이미지 경로 형식: /rooms/{roomId}/{filename}
     * @return 생성된 Room ID
     */
    @Transactional
    public Long createRoomWithImages(
            RoomCreateRequest request,
            OperationPolicy operationPolicy,
            RoomRule roomRule,
            Long refundPolicyId,
            TempImageFiles imageFiles
    ) {
        // 1. Room 엔티티 생성 및 저장
        Room room = roomMapper.toEntity(request, operationPolicy, roomRule, refundPolicyId);
        roomRepository.save(room);

        Long roomId = room.getId();

        // 2. 파일명 + roomId로 최종 경로 조립
        String mainPath = imageFiles.buildMainPath(roomId);
        String thumbnailPath = imageFiles.buildThumbnailPath(roomId);
        List<String> generalPaths = imageFiles.buildGeneralPaths(roomId);

        // 3. RoomImage 엔티티 생성 (cascade로 자동 저장)
        RoomImage.create(room, mainPath, RoomImage.ImageType.MAIN, 0);
        RoomImage.create(room, thumbnailPath, RoomImage.ImageType.THUMBNAIL, 0);
        for (int i = 0; i < generalPaths.size(); i++) {
            RoomImage.create(room, generalPaths.get(i), RoomImage.ImageType.GENERAL, i + 1);
        }

        return roomId;
    }

    // ========== Room 수정 ==========

    /**
     * Room 기본 정보만 수정 (이미지 변경 없음)
     */
    @Transactional
    public void updateRoomBasic(
            Long roomId,
            RoomUpdateRequest request,
            OperationPolicy operationPolicy,
            RoomRule roomRule,
            Long refundPolicyId
    ) {
        Room room = findRoomWithImages(roomId);
        room.updateRoom(
                request.name(), request.price(), request.maxCapacity(),
                operationPolicy, roomRule, refundPolicyId, request.amenities()
        );
    }

    /**
     * Room 수정 + 메인 이미지 교체
     * @return 삭제 대상 old 이미지 경로 목록
     */
    @Transactional
    public List<String> updateRoomWithMainImage(
            Long roomId,
            RoomUpdateRequest request,
            OperationPolicy operationPolicy,
            RoomRule roomRule,
            Long refundPolicyId,
            String newMainPath,
            String newThumbPath
    ) {
        Room room = findRoomWithImages(roomId);

        // 기본 정보 업데이트
        room.updateRoom(
                request.name(), request.price(), request.maxCapacity(),
                operationPolicy, roomRule, refundPolicyId, request.amenities()
        );

        // old 파일 경로 수집
        List<String> oldPaths = collectMainAndThumbnailPaths(room);

        // 엔티티 교체 (orphanRemoval로 DB에서 삭제)
        room.removeImagesByType(RoomImage.ImageType.MAIN);
        room.removeImagesByType(RoomImage.ImageType.THUMBNAIL);
        RoomImage.create(room, newMainPath, RoomImage.ImageType.MAIN, 0);
        RoomImage.create(room, newThumbPath, RoomImage.ImageType.THUMBNAIL, 0);

        return oldPaths;
    }

    /**
     * Room 수정 + 일반 이미지 추가
     */
    @Transactional
    public void updateRoomWithGeneralImages(
            Long roomId,
            RoomUpdateRequest request,
            OperationPolicy operationPolicy,
            RoomRule roomRule,
            Long refundPolicyId,
            List<String> newGeneralPaths
    ) {
        Room room = findRoomWithImages(roomId);

        // 기본 정보 업데이트
        room.updateRoom(
                request.name(), request.price(), request.maxCapacity(),
                operationPolicy, roomRule, refundPolicyId, request.amenities()
        );

        // 개수 제한 확인
        validateGeneralImageLimit(room, newGeneralPaths.size());

        // 새 이미지 엔티티 추가
        int maxSortOrder = getMaxGeneralSortOrder(room);
        for (int i = 0; i < newGeneralPaths.size(); i++) {
            RoomImage.create(room, newGeneralPaths.get(i), RoomImage.ImageType.GENERAL, maxSortOrder + i + 1);
        }
    }

    /**
     * Room 수정 + 메인 이미지 교체 + 일반 이미지 추가
     * @return 삭제 대상 old 이미지 경로 목록
     */
    @Transactional
    public List<String> updateRoomWithAllImages(
            Long roomId,
            RoomUpdateRequest request,
            OperationPolicy operationPolicy,
            RoomRule roomRule,
            Long refundPolicyId,
            String newMainPath,
            String newThumbPath,
            List<String> newGeneralPaths
    ) {
        Room room = findRoomWithImages(roomId);

        // 기본 정보 업데이트
        room.updateRoom(
                request.name(), request.price(), request.maxCapacity(),
                operationPolicy, roomRule, refundPolicyId, request.amenities()
        );

        // old 메인/썸네일 경로 수집
        List<String> oldPaths = collectMainAndThumbnailPaths(room);

        // 메인/썸네일 교체
        room.removeImagesByType(RoomImage.ImageType.MAIN);
        room.removeImagesByType(RoomImage.ImageType.THUMBNAIL);
        RoomImage.create(room, newMainPath, RoomImage.ImageType.MAIN, 0);
        RoomImage.create(room, newThumbPath, RoomImage.ImageType.THUMBNAIL, 0);

        // 일반 이미지 추가
        validateGeneralImageLimit(room, newGeneralPaths.size());
        int maxSortOrder = getMaxGeneralSortOrder(room);
        for (int i = 0; i < newGeneralPaths.size(); i++) {
            RoomImage.create(room, newGeneralPaths.get(i), RoomImage.ImageType.GENERAL, maxSortOrder + i + 1);
        }

        return oldPaths;
    }

    // ========== 상태 변경 ==========

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
    public void softDeleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        room.softDelete();
    }

    // ========== 내부 헬퍼 ==========

    private Room findRoomWithImages(Long roomId) {
        return roomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
    }

    private List<String> collectMainAndThumbnailPaths(Room room) {
        return room.getImages().stream()
                .filter(img -> img.getType() == RoomImage.ImageType.MAIN
                        || img.getType() == RoomImage.ImageType.THUMBNAIL)
                .map(RoomImage::getImageUrl)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private void validateGeneralImageLimit(Room room, int newCount) {
        long existingCount = room.getImages().stream()
                .filter(img -> img.getType() == RoomImage.ImageType.GENERAL)
                .count();

        if (existingCount + newCount > MAX_GENERAL_IMAGES) {
            throw new BusinessException(ErrorCode.ROOM_GENERAL_IMAGE_LIMIT_EXCEEDED);
        }
    }

    private int getMaxGeneralSortOrder(Room room) {
        return room.getImages().stream()
                .filter(img -> img.getType() == RoomImage.ImageType.GENERAL)
                .mapToInt(RoomImage::getSortOrder)
                .max()
                .orElse(0);
    }
}
