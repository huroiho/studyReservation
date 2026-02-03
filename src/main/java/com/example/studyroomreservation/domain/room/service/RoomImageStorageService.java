package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.global.exception.BusinessException;
import com.example.studyroomreservation.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Room 이미지 파일 저장 서비스
 * - 로컬 파일시스템 저장
 * - 썸네일 생성 (Thumbnailator 사용)
 * - 롤백 시 디렉토리 정리
 */
@Slf4j
@Service
public class RoomImageStorageService {

    private static final int THUMBNAIL_WIDTH = 320;
    private static final int THUMBNAIL_HEIGHT = 240;

    private final Path basePath;

    public RoomImageStorageService(
            @Value("${app.upload.base-path:./uploads}") String basePathStr
    ) {
        this.basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
    }

    /**
     * Room 디렉토리 생성
     */
    public void createRoomDirectory(Long roomId) {
        Path roomDir = getRoomDirectoryPath(roomId);
        try {
            Files.createDirectories(roomDir);
            log.debug("Created room directory: {}", roomDir);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "디렉토리 생성 실패: " + roomDir, e);
        }
    }

    /**
     * 메인 이미지 저장
     * @return DB에 저장할 상대 경로 (예: /rooms/15/main_uuid.jpg)
     */
    public String saveMainImage(Long roomId, MultipartFile file) {
        String uuid = UUID.randomUUID().toString();
        String extension = extensionFromContentType(file.getOriginalFilename());
        String filename = "main_" + uuid + "." + extension;

        return saveFile(roomId, file, filename);
    }

    /**
     * 메인 이미지로부터 썸네일 생성
     * @param mainImageRelativePath 메인 이미지의 상대 경로
     * @return 썸네일의 상대 경로
     */
    public String generateThumbnail(Long roomId, String mainImageRelativePath) {
        Path roomDir = getRoomDirectoryPath(roomId);

        // 상대경로에서 파일명만 뽑아서 roomDir 기준으로 다시 조립 (substring 제거)
        String mainFilename = Path.of(mainImageRelativePath).getFileName().toString();
        Path mainImagePath = roomDir.resolve(mainFilename);

        String uuid = UUID.randomUUID().toString();
        String thumbnailFilename = "thumb_" + uuid + ".jpg";
        Path thumbnailPath = roomDir.resolve(thumbnailFilename);

        try {
            Thumbnails.of(mainImagePath.toFile())
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .crop(Positions.CENTER)
                    .outputFormat("jpg")
                    .toFile(thumbnailPath.toFile());

            log.debug("Generated thumbnail: {}", thumbnailPath);
            return "/rooms/" + roomId + "/" + thumbnailFilename;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_THUMBNAIL_FAILED,
                    "썸네일 생성 실패: " + mainImageRelativePath, e);
        }
    }

    /**
     * 일반 이미지 저장 (다중)
     * @return 저장된 이미지들의 상대 경로 목록
     */
    public List<String> saveGeneralImages(Long roomId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<String> savedPaths = new ArrayList<>();
        int sortOrder = 1;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            String uuid = UUID.randomUUID().toString();
            String extension = extensionFromContentType(file.getOriginalFilename());
            String filename = "general_" + sortOrder + "_" + uuid + "." + extension;

            String relativePath = saveFile(roomId, file, filename);
            savedPaths.add(relativePath);
            sortOrder++;
        }

        return savedPaths;
    }

    /**
     * Room 디렉토리 삭제 (롤백용, best-effort)
     */
    public void deleteRoomDirectory(Long roomId) {
        Path roomDir = getRoomDirectoryPath(roomId);
        try {
            if (Files.exists(roomDir)) {
                // 디렉토리 내 모든 파일 삭제 후 디렉토리 삭제
                Files.walk(roomDir)
                        .sorted((a, b) -> b.compareTo(a)) // 역순 (파일 먼저, 디렉토리 나중)
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                log.warn("Failed to delete file during cleanup: {}", path, e);
                            }
                        });
                log.debug("Deleted room directory: {}", roomDir);
            }
        } catch (IOException e) {
            log.warn("Failed to cleanup room directory: {}", roomDir, e);
            // Best-effort: 실패해도 원본 예외를 마스킹하지 않음
        }
    }

    // === Private Methods ===

    private Path getRoomDirectoryPath(Long roomId) {
        return basePath.resolve("rooms").resolve(String.valueOf(roomId));
    }

    private String saveFile(Long roomId, MultipartFile file, String filename) {
        Path filePath = getRoomDirectoryPath(roomId).resolve(filename);

        try {
            Files.createDirectories(filePath.getParent());

            file.transferTo(filePath.toFile());
            log.debug("Saved image file: {}", filePath);
            return "/rooms/" + roomId + "/" + filename;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "파일 저장 실패: " + filename, e);
        }
    }

    private String extensionFromContentType(String contentType) {
        if (contentType == null) {
            // Validator에서 막히는 게 정상. 그래도 방어.
            return "jpg";
        }

        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
