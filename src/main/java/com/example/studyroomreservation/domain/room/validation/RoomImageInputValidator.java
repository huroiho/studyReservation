package com.example.studyroomreservation.domain.room.validation;

import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

/**
 * Room 이미지 입력 검증기 (stateless)
 * - Controller 레이어에서 사용
 * - 비즈니스 로직 없이 입력값만 검증
 * - Repository 의존 없음
 */
@Component
public class RoomImageInputValidator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_GENERAL_IMAGES = 10;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    /**
     * 룸 생성 시 이미지 검증
     * @throws BusinessException 검증 실패 시
     */
    public void validate(MultipartFile mainImage, List<MultipartFile> generalImages) {
        validateMainImage(mainImage);
        validateGeneralImages(generalImages);
    }

    /**
     * 수정 시 이미지 검증: mainImage가 없으면 기존 이미지를 유지
     * @throws BusinessException 검증 실패 시
     */
    public void validateForUpdate(MultipartFile mainImage, List<MultipartFile> generalImages) {
        if (mainImage != null && !mainImage.isEmpty()) {
            validateSingleImage(mainImage);
        }
        validateGeneralImages(generalImages);
    }

    private void validateMainImage(MultipartFile mainImage) {
        if (mainImage == null || mainImage.isEmpty()) {
            throw new BusinessException(ErrorCode.ROOM_MAIN_IMAGE_REQUIRED);
        }
        validateSingleImage(mainImage);
    }

    private void validateGeneralImages(List<MultipartFile> generalImages) {
        if (generalImages == null || generalImages.isEmpty()) {
            return;
        }

        List<MultipartFile> nonEmpty = generalImages.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();

        if (nonEmpty.size() > MAX_GENERAL_IMAGES) {
            throw new BusinessException(ErrorCode.ROOM_GENERAL_IMAGE_LIMIT_EXCEEDED);
        }

        nonEmpty.forEach(this::validateSingleImage);
    }

    private void validateSingleImage(MultipartFile file) {
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_TYPE_INVALID);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SIZE_EXCEEDED);
        }
    }
}
