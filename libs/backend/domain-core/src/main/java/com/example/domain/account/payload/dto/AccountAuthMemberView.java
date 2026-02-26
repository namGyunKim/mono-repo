package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;

/**
 * 인증/인가 처리 전용 회원 조회 DTO
 */
public record AccountAuthMemberView(
        Long id,
        String loginId,
        String password,
        String nickName,
        AccountRole role,
        MemberType memberType,
        MemberActiveStatus active,
        long tokenVersion
) {
    public static AccountAuthMemberView of(
            Long id,
            String loginId,
            String password,
            String nickName,
            AccountRole role,
            MemberType memberType,
            MemberActiveStatus active,
            long tokenVersion
    ) {
        return new AccountAuthMemberView(
                id,
                loginId,
                password,
                nickName,
                role,
                memberType,
                active,
                tokenVersion
        );
    }
}
