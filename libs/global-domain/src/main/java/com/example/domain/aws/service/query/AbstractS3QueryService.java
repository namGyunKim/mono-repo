package com.example.domain.aws.service.query;

import com.example.domain.aws.payload.dto.S3ImageUrlQuery;
import com.example.domain.aws.service.common.*;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.S3Client;

@Transactional(readOnly = true)
public abstract class AbstractS3QueryService extends AbstractS3ServiceSupport implements S3QueryService {

    private final S3ImageUrlSupport s3ImageUrlSupport;

    protected AbstractS3QueryService(
            S3Client s3Client,
            S3BucketResolver s3BucketResolver,
            S3KeyBuilder s3KeyBuilder,
            S3UrlParser s3UrlParser,
            S3ImageUrlSupport s3ImageUrlSupport
    ) {
        super(s3Client, s3BucketResolver, s3KeyBuilder, s3UrlParser);
        this.s3ImageUrlSupport = s3ImageUrlSupport;
    }

    @Override
    public String getImageUrl(S3ImageUrlQuery query) {
        return s3ImageUrlSupport.resolve(query);
    }
}
