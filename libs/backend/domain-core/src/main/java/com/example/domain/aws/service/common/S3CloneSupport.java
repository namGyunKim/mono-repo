package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import com.example.domain.aws.payload.dto.S3UrlParts;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.MetadataDirective;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class S3CloneSupport extends AbstractS3ServiceSupport {

    public S3CloneSupport(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
    }

    public String cloneFromUrl(String sourceS3Url, ImageType destinationImageType, Long destinationEntityId) {
        final long startNanos = System.nanoTime();
        logCloneStart(sourceS3Url, destinationImageType, destinationEntityId);
        S3UrlParts source = parseS3Url(sourceS3Url);
        String originalFilename = fetchOriginalFilename(source, sourceS3Url);
        String finalFileName = generateFinalUploadFileName(destinationImageType, originalFilename);
        String destinationKey = generateS3Key(finalFileName, destinationImageType, destinationEntityId);

        try {
            CopyObjectRequest copyRequest = buildCopyRequest(source, destinationKey, originalFilename);
            executeCopy(copyRequest);
            logCloneSuccess(source, destinationKey, destinationImageType, destinationEntityId, elapsedMillis(startNanos));
        } catch (Exception e) {
            logCloneFailure(sourceS3Url, source, destinationImageType, destinationEntityId, destinationKey, elapsedMillis(startNanos), e);
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED, "S3 간 복사에 실패했습니다.");
        }

        return finalFileName;
    }

    private void logCloneStart(String sourceS3Url, ImageType destinationImageType, Long destinationEntityId) {
        log.info(
                "traceId={}, S3-to-S3 복사 시작: sourceUrl={}, destType={}, destEntityId={}",
                TraceIdUtils.resolveTraceId(),
                sourceS3Url,
                destinationImageType.name(),
                destinationEntityId
        );
    }

    private void logCloneSuccess(
            S3UrlParts source,
            String destinationKey,
            ImageType destinationImageType,
            Long destinationEntityId,
            long elapsedMs
    ) {
        log.info(
                """
                        traceId={}, resultCode=SUCCESS, S3-to-S3 복사 완료: sourceBucket={}, sourceKey={}, destinationBucket={}, destinationKey={}, destinationImageType={}, destinationEntityId={}, elapsedMs={}
                        """.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                source.bucketName(),
                source.objectKey(),
                bucketName,
                destinationKey,
                destinationImageType.name(),
                destinationEntityId,
                elapsedMs
        );
    }

    private void logCloneFailure(
            String sourceS3Url,
            S3UrlParts source,
            ImageType destinationImageType,
            Long destinationEntityId,
            String destinationKey,
            long elapsedMs,
            Exception e
    ) {
        String sourceBucket = source != null ? source.bucketName() : "UNKNOWN";
        String sourceKey = source != null ? source.objectKey() : "UNKNOWN";
        log.error(
                """
                        traceId={}, errorCode={}, exceptionName={}, resultCode=FAILED, S3-to-S3 복사 실패: sourceUrl={}, sourceBucket={}, sourceKey={}, destinationBucket={}, destinationKey={}, destinationImageType={}, destinationEntityId={}, elapsedMs={}, errorMessage={}
                        """.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                ErrorCode.FILE_UPLOAD_FAILED.getCode(),
                e.getClass().getSimpleName(),
                sourceS3Url,
                sourceBucket,
                sourceKey,
                bucketName,
                destinationKey,
                destinationImageType.name(),
                destinationEntityId,
                elapsedMs,
                e.getMessage(),
                e
        );
    }

    private long elapsedMillis(long startNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    }

    private String fetchOriginalFilename(S3UrlParts source, String sourceS3Url) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(source.bucketName())
                    .key(source.objectKey())
                    .build();
            HeadObjectResponse headResponse = s3Client.headObject(headRequest);
            return extractFilenameFromContentDisposition(headResponse.contentDisposition());
        } catch (Exception e) {
            log.warn(
                    """
                            traceId={}, errorCode={}, exceptionName={}, S3 원본 파일 조회 실패: bucket={}, key={}, sourceUrl={}, errorMessage={}
                            """.stripTrailing(),
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.FILE_NOT_FOUND.getCode(),
                    e.getClass().getSimpleName(),
                    source.bucketName(),
                    source.objectKey(),
                    sourceS3Url,
                    e.getMessage(),
                    e
            );
            throw new GlobalException(ErrorCode.FILE_NOT_FOUND, "원본 S3 파일을 찾을 수 없거나 접근할 수 없습니다.");
        }
    }

    private CopyObjectRequest buildCopyRequest(S3UrlParts source, String destinationKey, String originalFilename) {
        return CopyObjectRequest.builder()
                .sourceBucket(source.bucketName())
                .sourceKey(source.objectKey())
                .destinationBucket(bucketName)
                .destinationKey(destinationKey)
                .contentDisposition(buildContentDisposition(originalFilename))
                .metadataDirective(MetadataDirective.REPLACE)
                .build();
    }

    private void executeCopy(CopyObjectRequest copyRequest) {
        s3Client.copyObject(copyRequest);
    }

}
