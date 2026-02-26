package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;

/**
 * S3 이미지 URL 조회 DTO
 */
public record S3ImageUrlQuery(
        String fileName,
        ImageType imageType,
        Long entityId
) {

    public static S3ImageUrlQuery of(String fileName, ImageType imageType, Long entityId) {
        return new S3ImageUrlQuery(fileName, imageType, entityId);
    }
}
