package com.example.domain.aws.payload.dto;

/**
 * S3 URL 파싱 결과 DTO
 *
 * <p>
 * - GEMINI 규칙: DTO는 record 사용
 * - 외부에서 new 호출을 금지하기 위해 정적 팩토리 메서드를 제공합니다.
 * </p>
 */
public record S3UrlParts(
        String bucketName,
        String objectKey
) {

    public static S3UrlParts of(String bucketName, String objectKey) {
        return new S3UrlParts(bucketName, objectKey);
    }
}
