package com.example.domain.aws.service.common;

import com.example.domain.aws.payload.dto.S3ImageUrlQuery;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class S3ImageUrlSupport extends AbstractS3ServiceSupport {

    public S3ImageUrlSupport(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
    }

    public String resolve(S3ImageUrlQuery query) {
        if (query == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "S3 이미지 URL 조회 요청 값이 비어있습니다.");
        }
        return buildPublicImageUrl(query.fileName(), query.imageType(), query.entityId());
    }
}
