package com.example.domain.security.port;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;

/**
 * Security 도메인이 토큰 발급/검증 시 필요한 회원 정보 DTO
 *
 * <p>
 * - Member 엔티티 직접 참조를 제거하고 Port를 통해 이 DTO를 전달합니다.
 * </p>
 */
public record SecurityMemberTokenInfo(
        Long id,
        String loginId,
        AccountRole role,
        String nickName,
        MemberType memberType,
        MemberActiveStatus active,
        long tokenVersion,
        String refreshTokenEncrypted
) {
    public static SecurityMemberTokenInfo of(
            Long id,
            String loginId,
            AccountRole role,
            String nickName,
            MemberType memberType,
            MemberActiveStatus active,
            long tokenVersion,
            String refreshTokenEncrypted
    ) {
        return new SecurityMemberTokenInfo(
                id, loginId, role, nickName, memberType, active,
                tokenVersion, refreshTokenEncrypted
        );
    }
}
