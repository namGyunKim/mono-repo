package com.example.domain.aws.payload.dto;

public record S3ImageUploadResult(
        Long memberImageId,
        String fileName,
        String imageUrl
) {

    public static S3ImageUploadResult of(Long memberImageId, String fileName, String imageUrl) {
        return new S3ImageUploadResult(memberImageId, fileName, imageUrl);
    }
}
