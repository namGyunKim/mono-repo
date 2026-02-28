package com.example.global.aop.support;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
public class ControllerResponseInfoResolver {

    public String getResponseStatus(final HttpServletResponse response) {
        if (response == null) {
            return "알 수 없음";
        }

        return String.valueOf(response.getStatus());
    }

    public String getResponseSize(final HttpServletResponse response) {
        if (response == null) {
            return "알 수 없음";
        }

        final String contentLength = response.getHeader("Content-Length");
        if (contentLength != null && !contentLength.isBlank()) {
            return contentLength + "B";
        }

        if (response instanceof ContentCachingResponseWrapper wrapper) {
            return wrapper.getContentSize() + "B";
        }

        return "알 수 없음";
    }
}
