package com.example.domain.aws.payload.dto;

import org.springframework.web.multipart.MultipartFile;

public record AdminS3ImageUploadCommand(
        Long memberId,
        MultipartFile file
) {

    public static AdminS3ImageUploadCommand of(Long memberId, MultipartFile file) {
        return new AdminS3ImageUploadCommand(memberId, file);
    }
}
