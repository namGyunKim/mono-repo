package com.example.global.logging;

import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;

public record RequestMeta(
        String traceId,
        String method,
        String path
) {
    public static RequestMeta from(final HttpServletRequest request) {
        final String traceId = TraceIdUtils.resolveTraceId();
        final String method = request != null ? request.getMethod() : "";
        final String path = request != null ? request.getRequestURI() : "";
        return new RequestMeta(traceId, method, path);
    }
}
