package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.S3UrlParts;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public abstract class AbstractS3ServiceSupport {

    protected final S3Client s3Client;
    private final S3BucketResolver s3BucketResolver;
    private final S3KeyBuilder s3KeyBuilder;
    private final S3UrlParser s3UrlParser;
    protected String bucketName;
    @Value("${aws.region}")
    protected String region;

    protected AbstractS3ServiceSupport(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser
    ) {
        this.s3Client = s3Client;
        this.s3BucketResolver = s3BucketResolver;
        this.s3KeyBuilder = s3KeyBuilder;
        this.s3UrlParser = s3UrlParser;
    }

    @PostConstruct
    public void init() {
        S3BucketSelection selection = s3BucketResolver.resolve();
        this.bucketName = selection.bucketName();
        logBucket(selection.localBucket());
    }

    private void logBucket(boolean localBucket) {
        String messageTemplate = localBucket ?
                "로컬 프로필이 감지되어 로컬 버킷({})을 사용합니다." :
                "운영 버킷({})을 사용합니다.";
        org.slf4j.LoggerFactory.getLogger(getClass())
                .info("traceId={}, " + messageTemplate, TraceIdUtils.resolveTraceId(), this.bucketName);
    }

    protected String generateFinalUploadFileName(ImageType imageType, String originalFilename) {
        return s3KeyBuilder.generateFinalUploadFileName(imageType, originalFilename);
    }

    protected void validateExtension(String originalFilename, ImageType imageType) {
        String fileExtension = getFileExtension(originalFilename);
        imageType.validateExtension(fileExtension);
    }

    protected String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        String clean = filename.split("\\?")[0];
        int lastDot = clean.lastIndexOf('.');
        if (lastDot == -1 || lastDot == clean.length() - 1) {
            return "";
        }
        return clean.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    protected String generateS3Key(String fileName, ImageType imageType, Long entityId) {
        return s3KeyBuilder.generateS3Key(fileName, imageType, entityId);
    }

    protected String buildPublicImageUrl(String fileName, ImageType imageType, Long entityId) {
        if (fileName == null || fileName.isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 URL 조회 요청 값이 비어있습니다.");
        }
        return s3KeyBuilder.buildPublicImageUrl(fileName, imageType, entityId, bucketName, region);
    }

    protected S3UrlParts parseS3Url(String s3Url) {
        return s3UrlParser.parseS3Url(s3Url, region);
    }

    protected String extractFilenameFromContentDisposition(String contentDisposition) {
        return s3UrlParser.extractFilenameFromContentDisposition(contentDisposition);
    }

    protected String buildContentDisposition(String originalFilename) {
        String encodedOriginalFilename = encodeFilename(originalFilename);
        return "attachment; filename*=\"UTF-8''" + encodedOriginalFilename + "\"";
    }

    protected String encodeFilename(String originalFilename) {
        return URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
