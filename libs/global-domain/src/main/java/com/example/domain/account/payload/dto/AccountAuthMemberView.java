package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;
import com.example.global.enums.GlobalActiveEnums;

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
        GlobalActiveEnums active,
        long tokenVersion
) {
    public static AccountAuthMemberView of(
            Long id,
            String loginId,
            String password,
            String nickName,
            AccountRole role,
            MemberType memberType,
            GlobalActiveEnums active,
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
