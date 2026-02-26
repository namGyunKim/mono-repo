package com.example.domain.aws.payload.response;

import com.example.domain.aws.payload.dto.S3ImageUploadResult;

public record S3ImageUploadResponse(
        Long memberImageId,
        String fileName,
        String imageUrl
) {

    public static S3ImageUploadResponse of(Long memberImageId, String fileName, String imageUrl) {
        return new S3ImageUploadResponse(memberImageId, fileName, imageUrl);
    }

    public static S3ImageUploadResponse from(S3ImageUploadResult result) {
        if (result == null) {
            throw new IllegalArgumentException("업로드 결과는 필수입니다.");
        }
        return of(result.memberImageId(), result.fileName(), result.imageUrl());
    }
}
