package com.example.global.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * OAuth 2.0 PKCE(Proof Key for Code Exchange) 유틸
 * <p>
 * - RFC 7636 준수
 * - code_verifier: 43~128 길이의 랜덤 문자열
 * - code_challenge: S256(SHA-256) 기반 Base64URL 인코딩(패딩 제거)
 */
public final class OAuthPkceUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private OAuthPkceUtils() {
    }

    /**
     * state 생성 (CSRF 방지)
     */
    public static String createState() {
        return randomBase64Url(32);
    }

    /**
     * nonce 생성 (OIDC ID Token 재사용/리플레이 방지)
     */
    public static String createNonce() {
        return randomBase64Url(32);
    }

    /**
     * PKCE code_verifier 생성
     * <p>
     * - 64 bytes 랜덤 -> Base64URL(패딩 제거) 시 길이가 충분히 길어 RFC 요구사항(43~128)을 만족합니다.
     */
    public static String createCodeVerifier() {
        return randomBase64Url(64);
    }

    /**
     * PKCE code_challenge(S256) 생성
     */
    public static String createCodeChallengeS256(final String codeVerifier) {
        if (codeVerifier == null || codeVerifier.isBlank()) {
            throw new IllegalArgumentException("codeVerifier는 필수입니다.");
        }

        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hashed = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (final NoSuchAlgorithmException e) {
            // Java 25 환경에서는 사실상 발생하지 않지만, 명시적으로 실패 원인을 드러냅니다.
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private static String randomBase64Url(final int byteLength) {
        final byte[] bytes = new byte[byteLength];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
