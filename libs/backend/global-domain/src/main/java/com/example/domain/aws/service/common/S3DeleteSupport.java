package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

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
        String s3Key = generateS3Key(fileName, imageType, entityId);
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();
        s3Client.deleteObject(deleteObjectRequest);
    }

    public void deleteBatch(List<String> fileNames, ImageType imageType, Long entityId) {
        if (fileNames == null || fileNames.isEmpty()) {
            return;
        }

        List<ObjectIdentifier> objectsToDelete = new ArrayList<>();
        for (String fileName : fileNames) {
            String key = generateS3Key(fileName, imageType, entityId);
            objectsToDelete.add(ObjectIdentifier.builder().key(key).build());
        }

        try {
            DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(Delete.builder().objects(objectsToDelete).build())
                    .build();

            DeleteObjectsResponse response = s3Client.deleteObjects(deleteObjectsRequest);
            if (response.hasErrors()) {
                log.error(
                        """
                                traceId={}, errorCode={}, exceptionName={}, S3 일부 파일 삭제 실패: bucket={}, imageType={}, entityId={}, fileCount={}, errors={}
                                """.stripTrailing(),
                        TraceIdUtils.resolveTraceId(),
                        ErrorCode.FAILED.getCode(),
                        "DeleteObjectsPartialFailure",
                        bucketName,
                        imageType,
                        entityId,
                        fileNames.size(),
                        response.errors()
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
                    bucketName,
                    imageType,
                    entityId,
                    fileNames.size(),
                    e
            );
            throw new GlobalException(ErrorCode.FAILED, "S3 일괄 삭제에 실패했습니다.");
        }
    }
}
