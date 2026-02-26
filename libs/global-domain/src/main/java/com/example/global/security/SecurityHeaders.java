package com.example.global.security;

/**
 * 보안 관련 헤더 상수
 */
public final class SecurityHeaders {

    public static final String AUTHORIZATION = "Authorization";
    public static final String REFRESH_TOKEN = "X-Refresh-Token";
    public static final String BEARER_PREFIX = "Bearer ";

    private SecurityHeaders() {
    }
}
