package com.example.global.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * HTTP 요청 URI 처리 유틸
 */
public final class RequestUriUtils {

    private RequestUriUtils() {
    }

    /**
     * 컨텍스트 경로를 제외한 애플리케이션 내부 경로를 반환합니다.
     *
     * @param request HTTP 요청
     * @return 컨텍스트 경로를 제외한 URI (예: /api/sessions)
     */
    public static String getPathWithinApplication(final HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        final String uri = request.getRequestURI();
        if (uri == null) {
            return "";
        }

        final String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }

        return uri;
    }
}
