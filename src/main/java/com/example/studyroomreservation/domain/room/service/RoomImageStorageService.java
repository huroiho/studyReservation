package com.example.studyroomreservation.domain.room.service;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Room 이미지 파일 저장 서비스
 * - 로컬 파일시스템 저장, 롤백 시 디렉토리 정리
 * - 썸네일 생성 (thumbnailator 사용)
 */
@Slf4j
@Service
public class RoomImageStorageService {

    private static final int THUMBNAIL_WIDTH = 320;
    private static final int THUMBNAIL_HEIGHT = 240;

    private final Path basePath;
    private final RoomImageContentValidator contentValidator;

    // 생성자 -> 루트 디렉토리 지정(application.yml에서 값 주입. 없으면 ./uploads)
    // 현재 룸에서만 이미지 업로드를 해서 룸 이미지 서비스에 있음.
    public RoomImageStorageService(
            @Value("${file.upload.base-path:./uploads}") String basePathStr,
            RoomImageContentValidator roomImageContentValidator
    ) {
        // 문자열을 Path 객체로 변환 -> 절대 경로로 변환 -> 정규화
        this.basePath = Paths.get(basePathStr).toAbsolutePath().normalize();
        this.contentValidator = roomImageContentValidator;
    }

    // room 디렉토리 생성
    public void  createRoomDirectory(Long roomId) {
        Path roomDir = getRoomDirectoryPath(roomId);
        try {
            Files.createDirectories(roomDir);   // mkdir -p
            log.debug("room 디렉토리 생성 성공: {}", roomDir);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "room 디렉토리 생성 실패: " + roomDir, e);
        }
    }

    /**
     * 메인 이미지 저장
     * @return DB에 저장할 상대 경로 (예: /rooms/15/main_uuid.jpg)
     */
    public String saveMainImage(Long roomId, MultipartFile file) {
        byte[] bytes = readBytesOrThrow(file);
        String mimeType = contentValidator.validateAndDetectMimeType(bytes);

        String uuid = UUID.randomUUID().toString(); // 이미지 파일명 중복 방지 + 원본 파일명 노출 방지
        String extension = extensionFromMimeType(mimeType);
        String filename = "main_" + uuid + "." + extension;

        return saveBytes(roomId, bytes, filename);
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

        // 이미지 하나씩 상대 경로 생성
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            byte[] bytes = readBytesOrThrow(file);
            String mimeType = contentValidator.validateAndDetectMimeType(bytes);

            String uuid = UUID.randomUUID().toString();
            String extension = extensionFromMimeType(mimeType);
            String filename = "general_" + sortOrder + "_" + uuid + "." + extension;

            savedPaths.add(saveBytes(roomId, bytes, filename));
            sortOrder++;
        }

        return savedPaths;
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

        // 썸네일이 저장될 경로를 먼저 db에 저장될 스트링으로 만들기
        String uuid = UUID.randomUUID().toString();
        String thumbnailFilename = "thumb_" + uuid + ".jpg";
        Path thumbnailPath = roomDir.resolve(thumbnailFilename);

        // 메인 이미지로 썸네일 생성 - thumbnailator
        try {
            Thumbnails.of(mainImagePath.toFile())
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .crop(Positions.CENTER)
                    .outputFormat("jpg")
                    .toFile(thumbnailPath.toFile());    // Path → java.io.File(실제 디스크의 파일 객체) 변환

            log.debug("썸네일 생성 성공 : {}", thumbnailPath);
            return "/rooms/" + roomId + "/" + thumbnailFilename;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_THUMBNAIL_FAILED,
                    "썸네일 생성 실패: " + mainImageRelativePath, e);
        }
    }

    /**
     * 개별 이미지 파일 삭제
     * @param relativePath DB에 저장된 상대 경로 (예: /rooms/15/main_uuid.jpg)
     */
    public void deleteImageFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        String normalized = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        Path filePath = basePath.resolve(normalized);

        try {
            Files.deleteIfExists(filePath);
            log.debug("이미지 파일 삭제: {}", filePath);
        } catch (IOException e) {
            log.warn("이미지 파일 삭제 실패: {}", filePath, e);
        }
    }

    // Room 디렉토리 삭제
    public void deleteRoomDirectory(Long roomId) {
        Path roomDir = getRoomDirectoryPath(roomId);
        if (!Files.exists(roomDir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(roomDir)) {
            paths
                    .sorted(Comparator.reverseOrder()) // 파일 먼저, 디렉토리 나중
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete file during cleanup: {}", path, e);
                        }
                    });

            log.debug("Deleted room directory: {}", roomDir);

        } catch (IOException e) {
            log.warn("Failed to cleanup room directory: {}", roomDir, e);
        }
    }

    // ========= 공통 메서드 ===========

    // roomId 기준으로 이미지 저장 경로(폴더) 생성 (/uploads/rooms/15)
    private Path getRoomDirectoryPath(Long roomId) {
        return basePath.resolve("rooms").resolve(String.valueOf(roomId)); // resolve는 경로를 이어붙이는 메서드.
    }

    private byte[] readBytesOrThrow(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED, "파일 읽기 실패", e);
        }
    }

    private String saveBytes(Long roomId, byte[] bytes, String filename) {

        Path filePath = getRoomDirectoryPath(roomId).resolve(filename); // 저장될 전체 경로

        try {
            Files.createDirectories(filePath.getParent());

            Files.write(filePath, bytes);           // 실제 디스크에 bytes 저장

            log.debug("파일 저장 성공: {}", filePath);
            return "/rooms/" + roomId + "/" + filename;

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.ROOM_IMAGE_SAVE_FAILED,
                    "파일 저장 실패: " + filename, e);
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
