package com.example.domain.aws.service.common;

public record S3BucketSelection(String bucketName, boolean localBucket) {

    public static S3BucketSelection of(String bucketName, boolean localBucket) {
        return new S3BucketSelection(bucketName, localBucket);
    }
}
