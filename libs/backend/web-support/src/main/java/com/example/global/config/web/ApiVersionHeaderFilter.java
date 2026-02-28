package com.example.global.config.web;

import com.example.global.config.web.support.ApiVersionErrorResponder;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.version.ApiVersioning;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * API-Version 헤더 필수 체크 필터
 *
 * <p>
 * - /api/** 요청에서 API-Version 헤더가 없는 경우 400 응답을 반환합니다.
 * - 예외: /api/health, /api/social/** (외부 OAuth Provider 콜백은 헤더 전달 불가)
 * </p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RequiredArgsConstructor
public class ApiVersionHeaderFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/";
    private static final String HEALTH_CHECK_PATH = "/api/health";
    private static final String SOCIAL_API_BASE_PATH = "/api/social";

    private final ApiVersionErrorResponder errorResponder;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request == null) {
            return true;
        }

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        final String path = getPathWithinApplication(request);
        if (!StringUtils.hasText(path) || !path.startsWith(API_PREFIX)) {
            return true;
        }

        return isExcludedPath(path);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        final String rawVersion = request.getHeader(ApiVersioning.HEADER_NAME);
        if (!StringUtils.hasText(rawVersion)) {
            errorResponder.writeErrorResponse(request, response, ErrorCode.API_VERSION_REQUIRED);
            return;
        }

        if (!ApiVersioning.isSupportedVersion(rawVersion)) {
            errorResponder.writeErrorResponse(request, response, ErrorCode.API_VERSION_INVALID);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        if (!StringUtils.hasText(path)) {
            return true;
        }

        if (HEALTH_CHECK_PATH.equals(path)) {
            return true;
        }

        if (SOCIAL_API_BASE_PATH.equals(path) || path.startsWith(SOCIAL_API_BASE_PATH + "/")) {
            return true;
        }

        return false;
    }

    private String getPathWithinApplication(HttpServletRequest request) {
        final String uri = request.getRequestURI();
        if (uri == null) {
            return "";
        }

        final String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }

        return uri;
    }
}
