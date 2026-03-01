package com.example.domain.security.port;

import com.example.domain.security.port.SecurityMemberTokenInfo;

import java.util.Optional;

/**
 * Security 도메인이 회원 토큰 관련 정보를 조회/변경할 때 사용하는 Port
 *
 * <p>
 * - Security 도메인은 MemberRepository / Member 엔티티를 직접 참조하지 않고 이 Port를 통해 접근합니다.
 * - Port 정의: security/port (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * </p>
 */
public interface SecurityMemberTokenPort {

    Optional<SecurityMemberTokenInfo> findTokenInfoById(Long memberId);

    Optional<SecurityMemberTokenInfo> findTokenInfoByLoginId(String loginId);

    /**
     * 리프레시 토큰 암호문을 갱신합니다.
     */
    void updateRefreshTokenEncrypted(Long memberId, String encrypted);

    /**
     * 토큰 버전을 회전하고 리프레시 토큰을 무효화합니다.
     */
    void revokeTokens(Long memberId);
}
