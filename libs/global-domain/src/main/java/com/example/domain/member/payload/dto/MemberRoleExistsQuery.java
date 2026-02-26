package com.example.domain.member.payload.dto;

import com.example.domain.account.enums.AccountRole;

public record MemberRoleExistsQuery(
        AccountRole role
) {

    public static MemberRoleExistsQuery of(AccountRole role) {
        return new MemberRoleExistsQuery(role);
    }
}
