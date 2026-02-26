package com.example.global.exception.support;

import com.example.global.exception.enums.ErrorCode;
import com.example.global.version.ApiVersioning;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ApiVersionErrorResolver {

    public ErrorCode resolve(HttpServletRequest request, ErrorCode fallback) {
        if (request == null) {
            return fallback;
        }

        String requestPath = request.getRequestURI();
        if (requestPath == null || !requestPath.startsWith("/api/")) {
            return fallback;
        }

        if ("/api/health".equals(requestPath)) {
            return fallback;
        }

        if ("/api/social".equals(requestPath) || requestPath.startsWith("/api/social/")) {
            return fallback;
        }

        String rawVersion = request.getHeader(ApiVersioning.HEADER_NAME);
        if (rawVersion == null || rawVersion.isBlank()) {
            return ErrorCode.API_VERSION_REQUIRED;
        }

        String version = rawVersion.trim();
        if (!ApiVersioning.isSupportedVersion(version)) {
            return ErrorCode.API_VERSION_INVALID;
        }

        return fallback;
    }
}
