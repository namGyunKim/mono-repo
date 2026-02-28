package com.example.domain.account.support;

import com.example.domain.account.payload.response.RefreshTokenResponse;

/**
 * account → security 도메인 경계를 넘는 토큰 갱신 포트
 * <p>
 * AccountAuthApiController가 JwtTokenRefreshCommandService에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface AccountTokenRefreshPort {

    RefreshTokenResponse refreshTokens(String refreshToken);
}
