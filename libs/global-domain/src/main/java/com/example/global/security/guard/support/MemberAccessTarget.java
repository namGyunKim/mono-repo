package com.example.global.security.guard.support;

import com.example.domain.account.enums.AccountRole;
import com.example.global.enums.GlobalActiveEnums;

public record MemberAccessTarget(
        AccountRole role,
        Long id,
        GlobalActiveEnums active
) {

    public static MemberAccessTarget of(AccountRole role, Long id, GlobalActiveEnums active) {
        return new MemberAccessTarget(role, id, active);
    }
}
