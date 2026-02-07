package com.example.studyroomreservation.domain.room.dto;

import java.util.List;

public record TempImageFiles(
        String tempDirId,
        String mainFilename,
        String thumbnailFilename,
        List<String> generalFilenames
) {
    private static final String PATH_FORMAT = "/rooms/%d/%s";

    public String buildMainPath(Long roomId) {
        return String.format(PATH_FORMAT, roomId, mainFilename);
    }

    public String buildThumbnailPath(Long roomId) {
        return String.format(PATH_FORMAT, roomId, thumbnailFilename);
    }

    public List<String> buildGeneralPaths(Long roomId) {
        return generalFilenames.stream()
                .map(filename -> String.format(PATH_FORMAT, roomId, filename))
                .toList();
    }
}
