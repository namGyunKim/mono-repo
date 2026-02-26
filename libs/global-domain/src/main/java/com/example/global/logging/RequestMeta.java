package com.example.global.logging;

import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;

public record RequestMeta(
        String traceId,
        String method,
        String path
) {
    public static RequestMeta from(HttpServletRequest request) {
        String traceId = TraceIdUtils.resolveTraceId();
        String method = request != null ? request.getMethod() : "";
        String path = request != null ? request.getRequestURI() : "";
        return new RequestMeta(traceId, method, path);
    }
}
