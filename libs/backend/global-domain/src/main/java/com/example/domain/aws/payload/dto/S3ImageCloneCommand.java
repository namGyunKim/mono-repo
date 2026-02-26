package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;

/**
 * S3 단일 이미지 복사 명령 DTO
 */
public record S3ImageCloneCommand(
        String sourceS3Url,
        ImageType destinationImageType,
        Long destinationEntityId
) {

    public static S3ImageCloneCommand of(String sourceS3Url, ImageType destinationImageType, Long destinationEntityId) {
        return new S3ImageCloneCommand(sourceS3Url, destinationImageType, destinationEntityId);
    }
}
