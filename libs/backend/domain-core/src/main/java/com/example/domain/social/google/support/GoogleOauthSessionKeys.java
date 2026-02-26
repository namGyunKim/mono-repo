package com.example.domain.social.google.support;

/**
 * Google OAuth 플로우에서 사용하는 Session Key 모음
 *
 * <p>
 * - HttpSession: 로그인 시작(/api/social/google/login) 시 생성한 OAuth 세션을 저장합니다.
 * </p>
 */
public final class GoogleOauthSessionKeys {

    /**
     * HttpSession 저장 키
     */
    public static final String SESSION_KEY = "OAUTH_GOOGLE_SESSION";

    private GoogleOauthSessionKeys() {
    }
}
