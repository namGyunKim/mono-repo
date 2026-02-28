package com.example.domain.account.support;

/**
 * account → security 도메인 경계를 넘는 토큰 폐기 포트
 * <p>
 * AccountCommandService가 JwtTokenRevocationCommandService에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface AccountTokenRevocationPort {

    void revokeOnLogout(Long memberId, String accessToken);
}
