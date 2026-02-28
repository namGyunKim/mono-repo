package com.example.domain.aws.service.common;

import com.example.domain.aws.payload.dto.S3UrlParts;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class S3UrlParser {

    public S3UrlParts parseS3Url(String s3Url, String region) {
        try {
            URI uri = URI.create(s3Url);
            URL url = uri.toURL();
            String host = url.getHost();
            String path = url.getPath();
            String key = path.substring(1);

            String s3Suffix = ".s3.%s.amazonaws.com".formatted(region);
            String bucket;
            int s3Index = host.indexOf(s3Suffix);
            if (s3Index != -1) {
                bucket = host.substring(0, s3Index);
            } else {
                throw new IllegalArgumentException("잘못된 S3 URL 형식입니다.");
            }
            return S3UrlParts.of(bucket, key);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.INPUT_VALUE_INVALID, "잘못된 S3 URL입니다.");
        }
    }

    public String extractFilenameFromContentDisposition(String contentDisposition) {
        if (contentDisposition == null) {
            return generateFallbackFilename();
        }

        String utf8Result = tryParseUtf8Filename(contentDisposition);
        if (utf8Result != null) {
            return utf8Result;
        }

        String plainResult = tryParsePlainFilename(contentDisposition);
        if (plainResult != null) {
            return plainResult;
        }

        return generateFallbackFilename();
    }

    private String tryParseUtf8Filename(String contentDisposition) {
        String prefix = "filename*=\"UTF-8''";
        int startIndex = contentDisposition.indexOf(prefix);
        if (startIndex == -1) {
            return null;
        }
        String encodedName = contentDisposition.substring(startIndex + prefix.length());
        if (encodedName.endsWith("\"")) {
            encodedName = encodedName.substring(0, encodedName.length() - 1);
        }
        try {
            return URLDecoder.decode(encodedName, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return generateFallbackFilename();
        }
    }

    private String tryParsePlainFilename(String contentDisposition) {
        String fnPrefix = "filename=";
        int startIndex = contentDisposition.indexOf(fnPrefix);
        if (startIndex == -1) {
            return null;
        }
        String name = contentDisposition.substring(startIndex + fnPrefix.length());
        if (name.startsWith("\"")) {
            name = name.substring(1);
        }
        if (name.endsWith("\"")) {
            name = name.substring(0, name.length() - 1);
        }
        return name.replace("+", "%20");
    }

    private String generateFallbackFilename() {
        return "cloned-file-%d".formatted(System.currentTimeMillis());
    }
}
