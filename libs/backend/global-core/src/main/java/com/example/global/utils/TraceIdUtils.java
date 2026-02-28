package com.example.global.utils;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceId 조회 유틸리티
 * - MDC에 저장된 traceId를 안전하게 반환합니다.
 */
public final class TraceIdUtils {

    public static final String TRACE_ID_KEY = "traceId";
    public static final String TRACE_HEADER_NAME = "X-Trace-Id";

    private TraceIdUtils() {
    }

    public static String resolveTraceId() {
        final String existing = MDC.get(TRACE_ID_KEY);
        if (existing != null && !existing.isBlank()) {
            return existing;
        }

        final String newTraceId = createTraceId();
        MDC.put(TRACE_ID_KEY, newTraceId);
        return newTraceId;
    }

    public static String createTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public static String resolveTraceIdFromRequest(final jakarta.servlet.http.HttpServletRequest request) {
        if (request == null) {
            return createTraceId();
        }

        final String headerTraceId = request.getHeader(resolveTraceHeaderName());
        if (headerTraceId != null && !headerTraceId.isBlank()) {
            return headerTraceId.trim();
        }

        return createTraceId();
    }

    public static String resolveTraceHeaderName() {
        return TRACE_HEADER_NAME;
    }
}
