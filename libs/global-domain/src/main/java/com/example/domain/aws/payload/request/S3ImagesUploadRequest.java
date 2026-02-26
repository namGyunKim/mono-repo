package com.example.domain.aws.payload.request;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record S3ImagesUploadRequest(
        @NotEmpty(message = "업로드 파일 목록은 필수입니다.")
        List<MultipartFile> files
) {

    public S3ImagesUploadRequest {
        if (files != null) {
            files = List.copyOf(files);
        }
    }

    public static S3ImagesUploadRequest of(List<MultipartFile> files) {
        return new S3ImagesUploadRequest(files);
    }
}
