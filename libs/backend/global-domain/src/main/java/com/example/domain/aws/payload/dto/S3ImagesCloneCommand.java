package com.example.domain.aws.payload.dto;

import com.example.domain.aws.enums.ImageType;

import java.util.List;

/**
 * S3 다중 이미지 복사 명령 DTO
 */
public record S3ImagesCloneCommand(
        List<String> sourceS3Urls,
        ImageType destinationImageType,
        Long destinationEntityId
) {

    public S3ImagesCloneCommand {
        if (sourceS3Urls != null) {
            sourceS3Urls = List.copyOf(sourceS3Urls);
        }
    }

    public static S3ImagesCloneCommand of(List<String> sourceS3Urls, ImageType destinationImageType, Long destinationEntityId) {
        return new S3ImagesCloneCommand(sourceS3Urls, destinationImageType, destinationEntityId);
    }
}
