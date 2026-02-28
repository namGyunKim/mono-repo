package com.example.global.utils;

import com.example.global.payload.response.ApiErrorDetail;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

/**
 * 로그인 관련 로그 처리 유틸
 */
public final class LoginLoggingUtils {

    public static final String DEFAULT_UNKNOWN_LOGIN_ID = "UNKNOWN";

    private LoginLoggingUtils() {
    }

    public static Optional<String> extractLoginId(HttpServletRequest request, String paramName, String attributeName) {
        if (request == null) {
            return Optional.empty();
        }

        final String loginId = request.getParameter(paramName);
        if (hasText(loginId)) {
            return Optional.of(loginId);
        }

        final Object attribute = request.getAttribute(attributeName);
        if (attribute instanceof String attrLoginId && hasText(attrLoginId)) {
            return Optional.of(attrLoginId);
        }

        return Optional.empty();
    }

    public static String resolveLoginIdOrDefault(
            HttpServletRequest request,
            String paramName,
            String attributeName,
            String defaultValue
    ) {
        return extractLoginId(request, paramName, attributeName).orElse(defaultValue);
    }

    public static String formatErrors(List<ApiErrorDetail> errors) {
        if (errors == null || errors.isEmpty()) {
            return "[]";
        }

        final int limit = Math.min(errors.size(), 5);
        final StringBuilder sb = new StringBuilder();
        sb.append('[');

        for (int i = 0; i < limit; i++) {
            final ApiErrorDetail error = errors.get(i);
            if (error == null) {
                continue;
            }

            if (sb.length() > 1) {
                sb.append(", ");
            }

            sb.append(safe(error.field())).append('=').append(safe(error.reason()));
        }

        if (errors.size() > limit) {
            sb.append(", ...");
        }

        sb.append(']');
        return sb.toString();
    }

    public static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
