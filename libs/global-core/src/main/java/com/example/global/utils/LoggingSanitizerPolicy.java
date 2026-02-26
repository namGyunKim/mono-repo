package com.example.global.utils;

import java.util.Locale;
import java.util.Set;

/**
 * 로깅 시 민감 필드 마스킹 정책
 *
 * <p>
 * - authorization/authorizationHeader 계열 필드는 정책상 마스킹 대상입니다.
 * </p>
 */
public final class LoggingSanitizerPolicy {

    private static final Set<String> SENSITIVE_FIELD_NAMES = Set.of(
            "password",
            "passwd",
            "pass",
            "accessToken",
            "refreshToken",
            "token",
            "authorization",
            "clientSecret",
            "secretKey",
            "idToken",
            "state",
            "nonce",
            "codeVerifier",
            "codeChallenge"
    );

    // 키워드 규칙은 "명시 키" 보완 목적이며, 일부 항목이 겹쳐도 정책적으로 허용합니다.
    private static final Set<String> SENSITIVE_FIELD_KEYWORDS = Set.of(
            "password",
            "token",
            "authorization",
            "secret",
            "nonce",
            "state"
    );

    private LoggingSanitizerPolicy() {
    }

    public static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) {
            return false;
        }

        String key = fieldName.trim();
        if (SENSITIVE_FIELD_NAMES.contains(key)) {
            return true;
        }

        String lower = key.toLowerCase(Locale.ROOT);
        if (SENSITIVE_FIELD_NAMES.contains(lower)) {
            return true;
        }
        return matchesKeyword(lower);
    }

    public static Set<String> getSensitiveFieldNames() {
        return SENSITIVE_FIELD_NAMES;
    }

    private static boolean matchesKeyword(String lower) {
        if (lower == null || lower.isBlank()) {
            return false;
        }
        if (lower.endsWith("token")) {
            return true;
        }
        for (String keyword : SENSITIVE_FIELD_KEYWORDS) {
            if (keyword.equals("secret") && isSecretBoundary(lower)) {
                return true;
            }
            if (!keyword.equals("secret") && lower.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSecretBoundary(String lower) {
        return lower.startsWith("secret") || lower.endsWith("secret");
    }
}
