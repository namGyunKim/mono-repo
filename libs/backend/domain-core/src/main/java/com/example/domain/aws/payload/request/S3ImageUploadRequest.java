package com.example.domain.aws.payload.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record S3ImageUploadRequest(
        @NotNull(message = "업로드 파일은 필수입니다.")
        MultipartFile file
) {

    public static S3ImageUploadRequest of(MultipartFile file) {
        return new S3ImageUploadRequest(file);
    }
}
