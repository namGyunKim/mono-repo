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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
public class S3UploadSupport extends AbstractS3ServiceSupport {

    public S3UploadSupport(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
    }

    public String upload(MultipartFile file, ImageType imageType, Long entityId) {
        String originalFilename = resolveOriginalFilename(file);
        try {
            return uploadToS3Internal(file.getInputStream(), file.getSize(), originalFilename, imageType, entityId);
        } catch (IOException e) {
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
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String uploadToS3Internal(InputStream inputStream, long size, String originalFilename, ImageType imageType, Long entityId) {
        validateExtension(originalFilename, imageType);
        if (requiresTempFile(imageType, size)) {
            return uploadWithTempFile(inputStream, originalFilename, imageType, entityId);
        }
        return uploadDirectly(inputStream, size, originalFilename, imageType, entityId);
    }

    private String uploadWithTempFile(InputStream inputStream, String originalFilename, ImageType imageType, Long entityId) {
        File tempFile = null;
        try {
            tempFile = createTempFile(originalFilename);
            copyToTempFile(inputStream, tempFile);
            validateTempFileSize(tempFile, imageType);
            validateImageDimensions(tempFile, imageType);
            String finalFileName = generateFinalUploadFileName(imageType, originalFilename);
            PutObjectRequest putObjectRequest = buildPutObjectRequest(finalFileName, imageType, entityId, originalFilename);
            putObjectFromFile(putObjectRequest, tempFile);
            return finalFileName;
        } catch (IOException e) {
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
                    (tempFile != null) ? tempFile.getAbsolutePath() : "UNKNOWN",
                    e
            );
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED);
        } finally {
            cleanupTempFile(tempFile);
        }
    }

    private String uploadDirectly(InputStream inputStream, long size, String originalFilename, ImageType imageType, Long entityId) {
        validateFileSize(size, imageType);
        String finalFileName = generateFinalUploadFileName(imageType, originalFilename);
        String s3Key = generateS3Key(finalFileName, imageType, entityId);

        try {
            PutObjectRequest putObjectRequest = buildPutObjectRequest(s3Key, originalFilename);
            putObjectFromStream(putObjectRequest, inputStream, size);
            return finalFileName;
        } catch (Exception e) {
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
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private String resolveOriginalFilename(MultipartFile file) {
        return file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
    }

    private boolean requiresTempFile(ImageType imageType, long size) {
        boolean needDimensionCheck = (imageType.getWidth() != null || imageType.getHeight() != null);
        boolean sizeUnknown = (size == -1);
        return needDimensionCheck || sizeUnknown;
    }

    private File createTempFile(String originalFilename) throws IOException {
        return File.createTempFile("upload_", "_" + originalFilename);
    }

    private void copyToTempFile(InputStream inputStream, File tempFile) throws IOException {
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void validateTempFileSize(File tempFile, ImageType imageType) {
        if (tempFile.length() > imageType.getMaxSize()) {
            throw new GlobalException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private void validateImageDimensions(File tempFile, ImageType imageType) {
        if (imageType.getWidth() == null && imageType.getHeight() == null) {
            return;
        }
        try {
            BufferedImage image = ImageIO.read(tempFile);
            if (image == null) {
                throw new GlobalException(ErrorCode.INVALID_IMAGE_FILE);
            }
            Integer width = image.getWidth();
            Integer height = image.getHeight();

            if ((imageType.getWidth() != null && !width.equals(imageType.getWidth())) ||
                    (imageType.getHeight() != null && !height.equals(imageType.getHeight()))) {
                throw new GlobalException(ErrorCode.INVALID_IMAGE_DIMENSIONS);
            }
        } catch (IOException e) {
            throw new GlobalException(ErrorCode.INVALID_IMAGE_FILE);
        }
    }

    private void validateFileSize(long size, ImageType imageType) {
        if (size > imageType.getMaxSize()) {
            throw new GlobalException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private PutObjectRequest buildPutObjectRequest(String finalFileName, ImageType imageType, Long entityId, String originalFilename) {
        String s3Key = generateS3Key(finalFileName, imageType, entityId);
        return buildPutObjectRequest(s3Key, originalFilename);
    }

    private PutObjectRequest buildPutObjectRequest(String s3Key, String originalFilename) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentDisposition(buildContentDisposition(originalFilename))
                .build();
    }

    private String buildContentDisposition(String originalFilename) {
        String encodedOriginalFilename = encodeFilename(originalFilename);
        return "attachment; filename*=\"UTF-8''" + encodedOriginalFilename + "\"";
    }

    private String encodeFilename(String originalFilename) {
        return URLEncoder.encode(originalFilename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private void putObjectFromFile(PutObjectRequest putObjectRequest, File tempFile) {
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(tempFile));
    }

    private void putObjectFromStream(PutObjectRequest putObjectRequest, InputStream inputStream, long size) {
        s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
    }

    private void cleanupTempFile(File tempFile) {
        if (tempFile == null || !tempFile.exists()) {
            return;
        }
        if (!tempFile.delete()) {
            log.warn(
                    """
                            traceId={}, 임시 파일 삭제 실패: tempFilePath={}
                            """.stripTrailing(),
                    TraceIdUtils.resolveTraceId(),
                    tempFile.getAbsolutePath()
            );
        }
    }
}
