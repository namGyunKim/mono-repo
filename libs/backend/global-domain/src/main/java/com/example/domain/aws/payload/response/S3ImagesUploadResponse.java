package com.example.domain.aws.payload.response;

import com.example.domain.aws.payload.dto.S3ImageUploadResult;

import java.util.List;

public record S3ImagesUploadResponse(
        List<S3ImageUploadResponse> items
) {

    public S3ImagesUploadResponse {
        if (items != null) {
            items = List.copyOf(items);
        }
    }

    public static S3ImagesUploadResponse of(List<S3ImageUploadResponse> items) {
        return new S3ImagesUploadResponse(items);
    }

    public static S3ImagesUploadResponse from(List<S3ImageUploadResult> results) {
        if (results == null) {
            throw new IllegalArgumentException("업로드 결과 목록은 필수입니다.");
        }
        return of(results.stream()
                .map(S3ImageUploadResponse::from)
                .toList());
    }
}
