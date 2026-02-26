package com.example.global.security.handler.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessMessageResolver {

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return "로그인 성공";
        }

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        String normalizedUri = requestUri;
        if (contextPath != null && !contextPath.isBlank() && requestUri != null && requestUri.startsWith(contextPath)) {
            normalizedUri = requestUri.substring(contextPath.length());
        }

        if (normalizedUri != null && normalizedUri.startsWith("/api/admin")) {
            return "관리자 로그인 성공";
        }

        return "일반 로그인 성공";
    }
}
