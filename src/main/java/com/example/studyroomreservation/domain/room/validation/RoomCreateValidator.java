package com.example.studyroomreservation.domain.room.validation;

import com.example.studyroomreservation.domain.refund.repository.RefundPolicyRepository;
import com.example.studyroomreservation.domain.room.dto.request.RoomCreateRequest;
import com.example.studyroomreservation.domain.room.repository.OperationPolicyRepository;
import com.example.studyroomreservation.domain.room.repository.RoomRuleRepository;
import com.example.studyroomreservation.global.common.BasePolicyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Room 생성 시 비즈니스 검증을 수행하는 Validator
 * - 정책 존재 및 활성화 상태 검증
 * - 이미지 유효성 검증 (크기, 타입, 해상도)
 *
 * Note: 이미지 검증은 validateWithImages() 메서드를 통해 수행
 */
@Component
@RequiredArgsConstructor
public class RoomCreateValidator implements Validator {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_DIMENSION = 8000;
    private static final int MAX_GENERAL_IMAGES = 10;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final OperationPolicyRepository operationPolicyRepository;
    private final RoomRuleRepository roomRuleRepository;
    private final RefundPolicyRepository refundPolicyRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return RoomCreateRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        // Bean Validation 후 정책 검증만 수행 (이미지는 validateImages에서)
        RoomCreateRequest request = (RoomCreateRequest) target;
        validatePolicies(request, errors);
    }

    //이미지 검증
    public void validateImages(
            MultipartFile mainImage,
            List<MultipartFile> generalImages,
            Errors errors
    ) {
        validateMainImage(mainImage, errors);
        validateGeneralImages(generalImages, errors);
    }

    private void validateMainImage(MultipartFile mainImage, Errors errors) {
        if (mainImage == null || mainImage.isEmpty()) {
            errors.rejectValue("mainImage", "RM004", "메인 이미지는 필수입니다.");
            return;
        }
        validateImageFile(mainImage, "mainImage", errors);
    }

    private void validateGeneralImages(List<MultipartFile> generalImages, Errors errors) {
        if (generalImages == null || generalImages.isEmpty()) {
            return;
        }

        // 빈 파일 필터링
        List<MultipartFile> nonEmptyImages = generalImages.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();

        if (nonEmptyImages.size() > MAX_GENERAL_IMAGES) {
            errors.rejectValue("generalImages","RM008","일반 이미지는 최대 " + MAX_GENERAL_IMAGES + "개까지 등록 가능합니다.");
            return;
        }

        for (int i = 0; i < nonEmptyImages.size(); i++) {
            validateImageFile(nonEmptyImages.get(i),"generalImages[" + i + "]", errors);
        }
    }

    private void validateImageFile(MultipartFile file, String fieldName, Errors errors) {
        // 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            errors.rejectValue(fieldName, "RM006","이미지 크기는 5MB를 초과할 수 없습니다.");
            return;
        }

        // 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            errors.rejectValue(fieldName,"RM005","이미지는 jpg, png, webp 형식만 가능합니다.");
            return;
        }

        // 해상도 검증
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) {
                errors.rejectValue(fieldName,"RM005","이미지 파일을 읽을 수 없습니다.");
                return;
            }
            if (img.getWidth() > MAX_DIMENSION || img.getHeight() > MAX_DIMENSION) {
                errors.rejectValue(fieldName,"RM007","이미지 크기는 8000x8000 픽셀을 초과할 수 없습니다.");
            }
        } catch (IOException e) {
            errors.rejectValue(fieldName,"RM005","이미지 파일을 읽을 수 없습니다.");
        }
    }

    private void validatePolicies(RoomCreateRequest request, Errors errors) {
        validateOperationPolicy(request.operationPolicyId(), errors);
        validateRoomRule(request.roomRuleId(), errors);
        validateRefundPolicy(request.refundPolicyId(), errors);
    }

    private void validateOperationPolicy(Long id, Errors errors) {
        if (id == null) return; // dto에서 notnull체크함.
        validateActive(
                operationPolicyRepository.findById(id).orElse(null),
                "operationPolicyId",
                "RM011",
                "선택한 운영 정책이 존재하지 않습니다.",
                "선택한 운영 정책이 비활성 상태입니다.",
                errors
        );
    }

    private void validateRoomRule(Long id, Errors errors) {
        if (id == null) return;
        validateActive(
                roomRuleRepository.findById(id).orElse(null),
                "roomRuleId",
                "RM012",
                "선택한 예약 규칙이 존재하지 않습니다.",
                "선택한 예약 규칙이 비활성 상태입니다.",
                errors
        );
    }

    private void validateRefundPolicy(Long id, Errors errors) {
        if (id == null) return;
        validateActive(
                refundPolicyRepository.findById(id).orElse(null),
                "refundPolicyId",
                "RM013",
                "선택한 환불 정책이 존재하지 않습니다.",
                "선택한 환불 정책이 비활성 상태입니다.",
                errors
        );
    }

    private void validateActive(
            BasePolicyEntity policy,
            String field,
            String code,
            String notFoundMsg,
            String inactiveMsg,
            Errors errors
    ) {
        if (policy == null) {
            errors.rejectValue(field, code, notFoundMsg);
            return;
        }
        if (!policy.isActive()) {
            errors.rejectValue(field, code, inactiveMsg);
        }
    }
}
