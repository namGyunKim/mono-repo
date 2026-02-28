package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class S3DeleteSupport extends AbstractS3ServiceSupport {

    public S3DeleteSupport(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
    }

    public void deleteSingle(String fileName, ImageType imageType, Long entityId) {
        final String s3Key = generateS3Key(fileName, imageType, entityId);
        final DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    public void deleteBatch(List<String> fileNames, ImageType imageType, Long entityId) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }

        final List<ObjectIdentifier> objectsToDelete = buildObjectIdentifiers(fileNames, imageType, entityId);
        executeDeleteObjects(objectsToDelete, imageType, entityId, fileNames.size());
    }

    private List<ObjectIdentifier> buildObjectIdentifiers(List<String> fileNames, ImageType imageType, Long entityId) {
        final List<ObjectIdentifier> identifiers = new ArrayList<>();
        for (final String fileName : fileNames) {
            final String key = generateS3Key(fileName, imageType, entityId);
            identifiers.add(ObjectIdentifier.builder().key(key).build());
        }
        return identifiers;
    }

    private void executeDeleteObjects(List<ObjectIdentifier> objectsToDelete, ImageType imageType, Long entityId, int fileCount) {
        try {
            final DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();

            final DeleteObjectsResponse response = s3Client.deleteObjects(request);
            if (response.hasErrors()) {
                log.error(
                        """
                                traceId={}, errorCode={}, exceptionName={}, S3 일부 파일 삭제 실패: bucket={}, imageType={}, entityId={}, fileCount={}, errors={}
                                """.stripTrailing(),
                        TraceIdUtils.resolveTraceId(),
                        ErrorCode.FAILED.getCode(),
                        "DeleteObjectsPartialFailure",
                        bucketName, imageType, entityId, fileCount, response.errors()
                );
                throw new GlobalException(ErrorCode.FAILED, "S3 일부 파일 삭제에 실패했습니다.");
            }
        } catch (GlobalException e) {
            throw e;
        } catch (Exception e) {
            log.error(
                    """
                            traceId={}, errorCode={}, exceptionName={}, S3 일괄 삭제 중 오류 발생: bucket={}, imageType={}, entityId={}, fileCount={}
                            """.stripTrailing(),
                    TraceIdUtils.resolveTraceId(),
                    ErrorCode.FAILED.getCode(),
                    e.getClass().getSimpleName(),
                    bucketName, imageType, entityId, fileCount, e
            );
            throw new GlobalException(ErrorCode.FAILED, "S3 일괄 삭제에 실패했습니다.");
        }
    }
}
