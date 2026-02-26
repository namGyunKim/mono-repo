package com.example.global.config.web.support;

import com.example.global.utils.LoggingSanitizerPolicy;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

@Component
public class FallbackRequestUriBuilder {

    public String buildUriWithQuery(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String uri = safe(request.getRequestURI());
        String query = buildSanitizedQueryString(request);
        if (!hasText(query)) {
            return uri;
        }

        return uri + "?" + query;
    }

    private String buildSanitizedQueryString(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        Enumeration<String> paramNames = request.getParameterNames();
        if (paramNames == null || !paramNames.hasMoreElements()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            if (!hasText(name)) {
                continue;
            }

            String[] values = request.getParameterValues(name);
            if (values == null || values.length == 0) {
                appendQueryPair(sb, name, maskIfSensitive(name, ""));
                continue;
            }

            for (String value : values) {
                appendQueryPair(sb, name, maskIfSensitive(name, value));
            }
        }
        return sb.toString();
    }

    private void appendQueryPair(StringBuilder sb, String name, String value) {
        if (sb.length() > 0) {
            sb.append('&');
        }
        String maskedName = maskKeyIfSensitive(name);
        sb.append(maskedName).append('=').append(safe(value));
    }

    private String maskIfSensitive(String name, String value) {
        if (LoggingSanitizerPolicy.isSensitiveField(name)) {
            return "***";
        }
        return value;
    }

    private String maskKeyIfSensitive(String name) {
        if (LoggingSanitizerPolicy.isSensitiveField(name)) {
            return "***";
        }
        return safe(name);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
