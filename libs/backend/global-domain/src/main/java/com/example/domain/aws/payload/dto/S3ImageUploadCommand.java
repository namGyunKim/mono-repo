package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;
import org.springframework.web.multipart.MultipartFile;

/**
 * S3 이미지 업로드 명령 DTO
 */
public record S3ImageUploadCommand(
        MultipartFile file,
        ImageType imageType,
        Long entityId
) {

    public static S3ImageUploadCommand of(MultipartFile file, ImageType imageType, Long entityId) {
        return new S3ImageUploadCommand(file, imageType, entityId);
    }
}
