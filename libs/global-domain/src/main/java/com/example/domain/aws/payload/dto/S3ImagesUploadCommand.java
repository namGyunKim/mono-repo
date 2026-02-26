package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * S3 다중 이미지 업로드 명령 DTO
 */
public record S3ImagesUploadCommand(
        List<MultipartFile> files,
        ImageType imageType,
        Long entityId
) {

    public S3ImagesUploadCommand {
        if (files != null) {
            files = List.copyOf(files);
        }
    }

    public static S3ImagesUploadCommand of(List<MultipartFile> files, ImageType imageType, Long entityId) {
        return new S3ImagesUploadCommand(files, imageType, entityId);
    }
}
