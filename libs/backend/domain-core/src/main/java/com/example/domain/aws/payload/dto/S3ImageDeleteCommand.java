package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;

import java.util.List;

/**
 * S3 이미지 삭제 명령 DTO
 */
public record S3ImageDeleteCommand(
        List<String> fileNames,
        ImageType imageType,
        Long entityId
) {

    public S3ImageDeleteCommand {
        if (fileNames != null) {
            fileNames = List.copyOf(fileNames);
        }
    }

    public static S3ImageDeleteCommand of(List<String> fileNames, ImageType imageType, Long entityId) {
        return new S3ImageDeleteCommand(fileNames, imageType, entityId);
    }
}
