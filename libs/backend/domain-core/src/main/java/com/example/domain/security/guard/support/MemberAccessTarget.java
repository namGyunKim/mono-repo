package com.example.domain.security.guard.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;

public record MemberAccessTarget(
        AccountRole role,
        Long id,
        MemberActiveStatus active
) {

    public static MemberAccessTarget of(AccountRole role, Long id, MemberActiveStatus active) {
        return new MemberAccessTarget(role, id, active);
    }
}
