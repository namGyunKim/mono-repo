package com.example.domain.aws.payload.dto;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record AdminS3ImagesUploadCommand(
        Long memberId,
        List<MultipartFile> files
) {

    public AdminS3ImagesUploadCommand {
        if (files != null) {
            files = List.copyOf(files);
        }
    }

    public static AdminS3ImagesUploadCommand of(Long memberId, List<MultipartFile> files) {
        return new AdminS3ImagesUploadCommand(memberId, files);
    }
}
