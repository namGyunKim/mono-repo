package com.example.domain.aws.service.common;

import com.example.domain.aws.enums.ImageType;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class S3KeyBuilder {

    public String generateFinalUploadFileName(ImageType imageType, String originalFilename) {
        return imageType.name() + "_" + originalFilename.replaceAll("\\s", "");
    }

    public String generateS3Key(String fileName, ImageType imageType, Long entityId) {
        String basePath = resolveBasePath(imageType);
        String encodedFileName = encodeFileName(fileName);
        return String.format("%s/%d/%s", basePath, entityId, encodedFileName);
    }

    public String buildPublicImageUrl(String fileName, ImageType imageType, Long entityId, String bucketName, String region) {
        String singleEncodedS3Key = generateS3Key(fileName, imageType, entityId);
        String doubleEncodedPath = singleEncodedS3Key.replace("%", "%25");
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, doubleEncodedPath);
    }

    private String resolveBasePath(ImageType imageType) {
        String path = imageType.getPath();
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    private String encodeFileName(String fileName) {
        return URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
