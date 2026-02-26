package com.example.global.security;

import org.springframework.http.HttpHeaders;

/**
 * Access/Refresh 토큰 응답 헤더 생성 유틸리티
 */
public final class TokenResponseHeaders {

    private TokenResponseHeaders() {
    }

    public static HttpHeaders of(String accessToken, String refreshToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(SecurityHeaders.AUTHORIZATION, SecurityHeaders.BEARER_PREFIX + accessToken);
        headers.add(SecurityHeaders.REFRESH_TOKEN, refreshToken);
        return headers;
    }
}
