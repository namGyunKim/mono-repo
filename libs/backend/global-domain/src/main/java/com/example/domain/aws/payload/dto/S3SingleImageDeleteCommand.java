package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;

/**
 * S3 단일 이미지 삭제 명령 DTO
 */
public record S3SingleImageDeleteCommand(
        String fileName,
        ImageType imageType,
        Long entityId
) {

    public static S3SingleImageDeleteCommand of(String fileName, ImageType imageType, Long entityId) {
        return new S3SingleImageDeleteCommand(fileName, imageType, entityId);
    }
}
