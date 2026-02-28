package com.example.domain.social.support;

import com.example.domain.account.payload.response.LoginTokenResponse;

/**
 * social → security 도메인 경계를 넘는 로그인 토큰 발급 포트
 * <p>
 * GoogleSocialRedirectCommandService가 LoginTokenCommandService에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface SocialLoginTokenPort {

    LoginTokenResponse issueTokens(Long memberId);
}
