package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;

/**
 * S3 엔티티 기준 이미지 일괄 삭제 명령 DTO
 */
public record S3ImageDeleteByEntityCommand(
        ImageType imageType,
        Long entityId
) {

    public static S3ImageDeleteByEntityCommand of(ImageType imageType, Long entityId) {
        return new S3ImageDeleteByEntityCommand(imageType, entityId);
    }
}
