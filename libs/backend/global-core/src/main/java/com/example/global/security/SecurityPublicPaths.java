package com.example.global.security;

import org.springframework.util.StringUtils;

/**
 * 보안 공개 경로 정책 상수
 *
 * <p>
 * - SecurityConfig와 SwaggerConfig가 동일한 공개 경로 기준을 공유하도록 관리합니다.
 * </p>
 */
public final class SecurityPublicPaths {

    public static final String[] PUBLIC_URLS = {
            // Swagger UI (/swagger-ui.html -> /swagger-ui/index.html)
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/sw.js",
            "/",
            "/favicon.ico",
            "/favicon.svg"
    };

    public static final String[] PUBLIC_API_URLS = {
            "/api/health",
            "/api/sessions",
            "/api/admin/sessions",
            // 리프레시 토큰 재발급은 만료된 AccessToken 상태에서도 호출되어야 하므로 공개 API로 유지합니다.
            "/api/tokens",
            "/api/social/**"
    };

    private static final String HEALTH_CHECK_PATH = "/api/health";
    private static final String USER_LOGIN_PATH = "/api/sessions";
    private static final String ADMIN_LOGIN_PATH = "/api/admin/sessions";
    private static final String TOKEN_REFRESH_PATH = "/api/tokens";
    private static final String SOCIAL_API_BASE_PATH = "/api/social";

    private SecurityPublicPaths() {
    }

    public static boolean isPublicApiPath(final String path) {
        if (!StringUtils.hasText(path)) {
            return false;
        }

        final String normalizedPath = path.trim();
        if (HEALTH_CHECK_PATH.equals(normalizedPath)) {
            return true;
        }
        if (USER_LOGIN_PATH.equals(normalizedPath) || ADMIN_LOGIN_PATH.equals(normalizedPath)) {
            return true;
        }
        if (TOKEN_REFRESH_PATH.equals(normalizedPath)) {
            return true;
        }
        return SOCIAL_API_BASE_PATH.equals(normalizedPath)
                || normalizedPath.startsWith(SOCIAL_API_BASE_PATH + "/");
    }
}
