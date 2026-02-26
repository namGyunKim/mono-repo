package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;
import com.example.global.enums.GlobalActiveEnums;

/**
 * 로그인/프로필 조회 전용 Projection DTO
 */
public record LoginMemberView(
        Long id,
        String loginId,
        AccountRole role,
        String nickName,
        MemberType memberType,
        GlobalActiveEnums active
) {

    public static LoginMemberView of(
            Long id,
            String loginId,
            AccountRole role,
            String nickName,
            MemberType memberType,
            GlobalActiveEnums active
    ) {
        return new LoginMemberView(id, loginId, role, nickName, memberType, active);
    }
}
