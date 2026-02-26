package com.example.domain.aws.payload.request;

import jakarta.validation.constraints.NotNull;

public record S3ImageDeleteRequest(
        @NotNull(message = "회원 이미지 ID는 필수입니다.")
        Long memberImageId
) {

    public static S3ImageDeleteRequest of(Long memberImageId) {
        return new S3ImageDeleteRequest(memberImageId);
    }
}
