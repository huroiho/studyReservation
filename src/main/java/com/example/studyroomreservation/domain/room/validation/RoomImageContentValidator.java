package com.example.studyroomreservation.domain.room.validation;

import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

@Component
public class RoomImageContentValidator {
    private static final Tika TIKA = new Tika();
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    public String validateAndDetectMimeType(byte[] bytes) {
        String mimeType = TIKA.detect(bytes);

        if (!ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_TYPE_INVALID);
        }

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new BusinessException(ErrorCode.ROOM_IMAGE_TYPE_INVALID);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_TYPE_INVALID, "이미지 타입 검증 실패", e);
        }

        return mimeType;
    }
}
