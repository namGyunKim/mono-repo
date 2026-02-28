package com.example.domain.aws.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class S3BucketResolver {

    private final Environment environment;
    private final String defaultBucketName;
    private final String localBucketName;

    public S3BucketResolver(
            Environment environment,
            @Value("${s3.bucket}") String defaultBucketName,
            @Value("${s3.bucket-local:}") String localBucketName
    ) {
        this.environment = environment;
        this.defaultBucketName = defaultBucketName;
        this.localBucketName = localBucketName;
    }

    public S3BucketSelection resolve() {
        final boolean localProfileActive = Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("local"));
        final boolean useLocalBucket = localProfileActive && localBucketName != null && !localBucketName.isBlank();
        final String bucketName = useLocalBucket ? localBucketName : defaultBucketName;
        return S3BucketSelection.of(bucketName, useLocalBucket);
    }
}
