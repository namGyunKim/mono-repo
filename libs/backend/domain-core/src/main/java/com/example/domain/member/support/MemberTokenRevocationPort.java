package com.example.domain.member.support;

import com.example.global.security.payload.SecurityLogoutCommand;

/**
 * member → security 도메인 경계를 넘는 토큰 폐기 포트
 * <p>
 * AdminMemberCommandService가 JwtTokenRevocationCommandService에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface MemberTokenRevocationPort {

    void revokeOnLogout(SecurityLogoutCommand command);
}
