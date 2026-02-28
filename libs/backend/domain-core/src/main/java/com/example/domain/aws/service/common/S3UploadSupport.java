package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * S3 파일 업로드 오케스트레이션을 담당하는 클래스
 *
 * <p>
 * [책임]
 * - S3 업로드 흐름 조율 (오케스트레이션)
 * - S3 PutObject 요청 생성 및 실행
 * </p>
 *
 * <p>
 * [위임]
 * - 파일/이미지 검증: {@link ImageUploadValidator}
 * - 임시 파일 관리: {@link TempFileHandler}
 * - 확장자 검증, S3 키 생성: {@link AbstractS3ServiceSupport}
 * </p>
 */
@Slf4j
@Component
public class S3UploadSupport extends AbstractS3ServiceSupport {

    private final ImageUploadValidator imageUploadValidator;
    private final TempFileHandler tempFileHandler;

    public S3UploadSupport(
            final S3Client s3Client,
            final S3BucketResolver s3BucketResolver,
            final S3KeyBuilder s3KeyBuilder,
            final S3UrlParser s3UrlParser,
            final ImageUploadValidator imageUploadValidator,
            final TempFileHandler tempFileHandler
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
        this.imageUploadValidator = imageUploadValidator;
        this.tempFileHandler = tempFileHandler;
    }

    public String upload(final MultipartFile file, final ImageType imageType, final Long entityId) {
        final String originalFilename = resolveOriginalFilename(file);
        try {
            return uploadToS3Internal(file.getInputStream(), file.getSize(), originalFilename, imageType, entityId);
        } catch (IOException e) {
            logUploadIOError(originalFilename, entityId, e);
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String uploadToS3Internal(
            final InputStream inputStream,
            final long size,
            final String originalFilename,
            final ImageType imageType,
            final Long entityId
    ) {
        validateExtension(originalFilename, imageType);

        if (requiresTempFile(imageType, size)) {
            return uploadWithTempFile(inputStream, originalFilename, imageType, entityId);
        }
        return uploadDirectly(inputStream, size, originalFilename, imageType, entityId);
    }

    private String uploadWithTempFile(
            final InputStream inputStream,
            final String originalFilename,
            final ImageType imageType,
            final Long entityId
    ) {
        File tempFile = null;
        try {
            tempFile = tempFileHandler.createTempFile(originalFilename);
            tempFileHandler.copyToTempFile(inputStream, tempFile);

            imageUploadValidator.validateTempFileSize(tempFile, imageType);
            imageUploadValidator.validateImageDimensions(tempFile, imageType);

            final String finalFileName = generateFinalUploadFileName(imageType, originalFilename);
            final String s3Key = generateS3Key(finalFileName, imageType, entityId);
            final PutObjectRequest putObjectRequest = buildPutObjectRequest(s3Key, originalFilename);

            putObjectFromFile(putObjectRequest, tempFile);
            return finalFileName;
        } catch (IOException e) {
            logTempFileError(originalFilename, imageType, entityId, tempFile, e);
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED);
        } finally {
            tempFileHandler.cleanup(tempFile);
        }
    }

    private String uploadDirectly(
            final InputStream inputStream,
            final long size,
            final String originalFilename,
            final ImageType imageType,
            final Long entityId
    ) {
        imageUploadValidator.validateFileSize(size, imageType);

        final String finalFileName = generateFinalUploadFileName(imageType, originalFilename);
        final String s3Key = generateS3Key(finalFileName, imageType, entityId);

        try {
            final PutObjectRequest putObjectRequest = buildPutObjectRequest(s3Key, originalFilename);
            putObjectFromStream(putObjectRequest, inputStream, size);
            return finalFileName;
        } catch (Exception e) {
            logStreamingUploadError(originalFilename, imageType, entityId, s3Key, size, e);
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String resolveOriginalFilename(final MultipartFile file) {
        final String filename = file.getOriginalFilename();
        return filename != null ? filename : "unknown";
    }

    private boolean requiresTempFile(final ImageType imageType, final long size) {
        final boolean needDimensionCheck = imageType.getWidth() != null || imageType.getHeight() != null;
        final boolean sizeUnknown = size == -1;
        return needDimensionCheck || sizeUnknown;
    }

    private PutObjectRequest buildPutObjectRequest(final String s3Key, final String originalFilename) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentDisposition(buildContentDisposition(originalFilename))
                .build();
    }

    private void putObjectFromFile(final PutObjectRequest putObjectRequest, final File tempFile) {
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempFile));
    }

    private void putObjectFromStream(final PutObjectRequest putObjectRequest, final InputStream inputStream, final long size) {
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
    }

    // ========== Logging Methods ==========

    private void logUploadIOError(final String originalFilename, final Long entityId, final IOException e) {
        log.error(
                """
                        traceId={}, errorCode={}, exceptionName={}, S3 파일 업로드 실패 (IO 오류): originalFilename={}, entityId={}
                        """.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                ErrorCode.FILE_UPLOAD_FAILED.getCode(),
                e.getClass().getSimpleName(),
                originalFilename,
                entityId,
                e
        );
    }

    private void logTempFileError(
            final String originalFilename,
            final ImageType imageType,
            final Long entityId,
            final File tempFile,
            final IOException e
    ) {
        log.error(
                """
                        traceId={}, errorCode={}, exceptionName={}, 임시 파일 처리 중 오류 발생: originalFilename={}, imageType={}, entityId={}, tempFilePath={}
                        """.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                ErrorCode.FILE_UPLOAD_FAILED.getCode(),
                e.getClass().getSimpleName(),
                originalFilename,
                imageType,
                entityId,
                tempFileHandler.getPathOrUnknown(tempFile),
                e
        );
    }

    private void logStreamingUploadError(
            final String originalFilename,
            final ImageType imageType,
            final Long entityId,
            final String s3Key,
            final long size,
            final Exception e
    ) {
        log.error(
                """
                        traceId={}, errorCode={}, exceptionName={}, S3 스트리밍 업로드 실패: originalFilename={}, imageType={}, entityId={}, s3Key={}, size={}
                        """.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                ErrorCode.FILE_UPLOAD_FAILED.getCode(),
                e.getClass().getSimpleName(),
                originalFilename,
                imageType,
                entityId,
                s3Key,
                size,
                e
        );
    }
}
