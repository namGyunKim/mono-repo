package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;
import com.example.global.enums.GlobalActiveEnums;

/**
 * 로그인 요청 정책 검증 전용 조회 DTO
 */
public record AccountLoginCandidateView(
        Long id,
        String loginId,
        AccountRole role,
        MemberType memberType,
        GlobalActiveEnums active
) {
    public static AccountLoginCandidateView of(
            Long id,
            String loginId,
            AccountRole role,
            MemberType memberType,
            GlobalActiveEnums active
    ) {
        return new AccountLoginCandidateView(id, loginId, role, memberType, active);
    }
}
