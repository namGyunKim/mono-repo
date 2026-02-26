package com.example.domain.social.google.payload.dto;

import com.example.global.utils.OAuthPkceUtils;

/**
 * 구글 OAuth 인증 플로우에서 1회성으로 사용되는 세션 데이터
 * <p>
 * - state: CSRF 방지
 * - nonce: OIDC ID Token 리플레이 방지
 * - codeVerifier/codeChallenge: PKCE
 *
 * <p>
 * [네이밍 규칙 표준화]
 * - create(): 내부에서 랜덤/시간 등 값을 생성하는 경우
 * - of(...): 외부 입력(혹은 저장된 값)으로 record를 명시적으로 구성하는 경우
 * </p>
 */
public record GoogleOauthSession(
        String state,
        String nonce,
        String codeVerifier,
        String codeChallenge,
        long issuedAtEpochMillis
) {

    /**
     * 명시적 생성(재구성)용 팩토리 메서드
     * <p>
     * - 테스트/리플레이/세션 복원 등에서 외부에서 new 호출을 피하기 위함입니다.
     */
    public static GoogleOauthSession of(
            String state,
            String nonce,
            String codeVerifier,
            String codeChallenge,
            long issuedAtEpochMillis
    ) {
        return new GoogleOauthSession(state, nonce, codeVerifier, codeChallenge, issuedAtEpochMillis);
    }

    /**
     * 신규 OAuth 세션 생성
     * <p>
     * - state/nonce/PKCE를 생성하고, issuedAtEpochMillis를 현재 시각으로 설정합니다.
     */
    public static GoogleOauthSession create() {
        String state = OAuthPkceUtils.createState();
        String nonce = OAuthPkceUtils.createNonce();
        String codeVerifier = OAuthPkceUtils.createCodeVerifier();
        String codeChallenge = OAuthPkceUtils.createCodeChallengeS256(codeVerifier);
        return of(state, nonce, codeVerifier, codeChallenge, System.currentTimeMillis());
    }
}
