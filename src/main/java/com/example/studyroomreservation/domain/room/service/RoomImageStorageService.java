package com.example.studyroomreservation.domain.room.service;

import com.example.studyroomreservation.domain.room.dto.TempImageFiles;
import com.example.studyroomreservation.domain.room.validation.RoomImageContentValidator;
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
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Room 이미지 파일 저장 서비스 (파일 I/O 전용)
 * - 임시 디렉토리에 파일 저장
 * - 임시 → 실제 디렉토리 파일 이동
 * - 파일/디렉토리 삭제
 */
@Slf4j
@Service
public class RoomImageStorageService {

    private static final int THUMBNAIL_WIDTH = 320;
    private static final int THUMBNAIL_HEIGHT = 240;
    private static final String TEMP_PREFIX = "temp_";

    private final Path basePath;
    private final RoomImageContentValidator contentValidator;

    public RoomImageStorageService(
            @Value("${file.upload.base-path:./uploads}") String basePathStr,
            RoomImageContentValidator roomImageContentValidator
    ) {
        this.basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
        this.contentValidator = roomImageContentValidator;
    }

    // ========== 임시 디렉토리 저장 ==========

    public TempImageFiles saveImagesToTemp(
            MultipartFile mainImage,
            List<MultipartFile> generalImages
    ) {
        String tempDirId = UUID.randomUUID().toString();
        Path tempDir = getTempDirectoryPath(tempDirId);

        try {
            Files.createDirectories(tempDir);

            // 메인 이미지 저장
            String mainFilename = saveFileToTemp(tempDir, mainImage, "main");

            // 썸네일 생성
            String thumbnailFilename = generateThumbnailInTemp(tempDir, mainFilename);

            // 일반 이미지 저장
            List<String> generalFilenames = new ArrayList<>();
            if (generalImages != null) {
                for (MultipartFile file : generalImages) {
                        generalFilenames.add(saveFileToTemp(tempDir, file, "general"));
                }
            }

            log.debug("임시 저장 완료: tempDirId={}, main={}, thumb={}, general={}",
                    tempDirId, mainFilename, thumbnailFilename, generalFilenames.size());

            return new TempImageFiles(tempDirId, mainFilename, thumbnailFilename, generalFilenames);

        } catch (IOException e) {
            // 부분 저장 실패 시 cleanup
            deleteTempDirectory(tempDirId);
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED, "임시 파일 저장 실패", e);
        }
    }

    // 임시 디렉토리의 파일들을 실제 room 디렉토리로 이
    public void moveToRoomDirectory(String tempDirId, Long roomId) {
        Path tempDir = getTempDirectoryPath(tempDirId);
        Path roomDir = getRoomDirectoryPath(roomId);

        if (!Files.exists(tempDir)) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "임시 디렉토리가 존재하지 않음: " + tempDirId);
        }

        try {
            Files.createDirectories(roomDir);

            try (Stream<Path> files = Files.list(tempDir)) {
                List<Path> fileList = files.toList();
                for (Path source : fileList) {
                    Path target = roomDir.resolve(source.getFileName());
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
                    log.debug("파일 이동: {} -> {}", source, target);
                }
            }

            Files.deleteIfExists(tempDir);
            log.debug("파일 이동 완료: tempDirId={} -> roomId={}", tempDirId, roomId);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "파일 이동 실패: tempDirId=" + tempDirId + ", roomId=" + roomId, e);
        }
    }

    // ========== Room 디렉토리 직접 저장 (수정 시 사용) ==========

    /**
     * 메인 이미지를 room 디렉토리에 직접 저장
     * @return 저장된 파일의 상대 경로 (/rooms/{roomId}/{filename})
     */
    public String saveMainImage(Long roomId, MultipartFile file) {
        Path roomDir = getRoomDirectoryPath(roomId);
        ensureDirectoryExists(roomDir);

        String filename = saveFileToDirectory(roomDir, file, "main");
        return buildRelativePath(roomId, filename);
    }

    /**
     * 메인 이미지로부터 썸네일 생성
     * @return 썸네일 상대 경로
     */
    public String generateThumbnail(Long roomId, String mainImagePath) {
        Path roomDir = getRoomDirectoryPath(roomId);
        String mainFilename = Path.of(mainImagePath).getFileName().toString();

        String thumbnailFilename = generateThumbnailFromFile(roomDir, mainFilename);
        return buildRelativePath(roomId, thumbnailFilename);
    }

    /**
     * 일반 이미지를 room 디렉토리에 저장
     * @return 저장된 파일들의 상대 경로 목록
     */
    public List<String> saveGeneralImages(Long roomId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        Path roomDir = getRoomDirectoryPath(roomId);
        ensureDirectoryExists(roomDir);

        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                String filename = saveFileToDirectory(roomDir, file, "general");
                paths.add(buildRelativePath(roomId, filename));
            }
        }
        return paths;
    }

    // ========== 삭제 ==========

    /**
     * 임시 디렉토리 삭제
     */
    public void deleteTempDirectory(String tempDirId) {
        deleteDirectoryRecursively(getTempDirectoryPath(tempDirId));
    }

    /**
     * Room 디렉토리 삭제
     */
    public void deleteRoomDirectory(Long roomId) {
        deleteDirectoryRecursively(getRoomDirectoryPath(roomId));
    }

    /**
     * 개별 이미지 파일 삭제
     */
    public void deleteImageFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        String normalized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path filePath = basePath.resolve(normalized);

        try {
            if (Files.deleteIfExists(filePath)) {
                log.debug("이미지 파일 삭제: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("이미지 파일 삭제 실패: {}", filePath, e);
        }
    }

    /**
     * 여러 이미지 파일 삭제
     */
    public void deleteImageFiles(List<String> paths) {
        if (paths == null) return;
        paths.forEach(this::deleteImageFile);
    }

    // ========== 내부 헬퍼 메서드 ==========

    private Path getRoomDirectoryPath(Long roomId) {
        return basePath.resolve("rooms").resolve(String.valueOf(roomId));
    }

    private Path getTempDirectoryPath(String tempDirId) {
        return basePath.resolve("rooms").resolve(TEMP_PREFIX + tempDirId);
    }

    private String buildRelativePath(Long roomId, String filename) {
        return "/rooms/" + roomId + "/" + filename;
    }

    private void ensureDirectoryExists(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "디렉토리 생성 실패: " + dir, e);
        }
    }

    private String saveFileToTemp(Path tempDir, MultipartFile file, String prefix) throws IOException {
        byte[] bytes = file.getBytes();
        String mimeType = contentValidator.validateAndDetectMimeType(bytes);
        String filename = prefix + "_" + UUID.randomUUID() + "." + extensionFromMimeType(mimeType);

        Files.write(tempDir.resolve(filename), bytes);
        return filename;
    }

    private String saveFileToDirectory(Path dir, MultipartFile file, String prefix) {
        try {
            byte[] bytes = file.getBytes();
            String mimeType = contentValidator.validateAndDetectMimeType(bytes);
            String filename = prefix + "_" + UUID.randomUUID() + "." + extensionFromMimeType(mimeType);

            Files.write(dir.resolve(filename), bytes);
            log.debug("파일 저장: {}/{}", dir, filename);
            return filename;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED, "파일 저장 실패", e);
        }
    }

    private String generateThumbnailInTemp(Path tempDir, String mainFilename) throws IOException {
        return generateThumbnailFromFile(tempDir, mainFilename);
    }

    private String generateThumbnailFromFile(Path dir, String mainFilename) {
        Path mainPath = dir.resolve(mainFilename);
        String thumbnailFilename = "thumb_" + UUID.randomUUID() + ".jpg";
        Path thumbnailPath = dir.resolve(thumbnailFilename);

        try {
            Thumbnails.of(mainPath.toFile())
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .crop(Positions.CENTER)
                    .outputFormat("jpg")
                    .toFile(thumbnailPath.toFile());

            log.debug("썸네일 생성: {}", thumbnailPath);
            return thumbnailFilename;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_THUMBNAIL_FAILED,
                    "썸네일 생성 실패: " + mainFilename, e);
        }
    }

    private void deleteDirectoryRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("파일 삭제 실패: {}", path, e);
                        }
                    });
            log.debug("디렉토리 삭제: {}", dir);
        } catch (IOException e) {
            log.warn("디렉토리 삭제 실패: {}", dir, e);
        }
    }

    private String extensionFromMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new BusinessException(ErrorCode.ROOM_IMAGE_TYPE_INVALID);
        };
    }
}
