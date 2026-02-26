package com.example.domain.security.guard.payload;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.CurrentAccountDTO;

public record MemberPermissionCheckCommand(
        CurrentAccountDTO currentAccount,
        AccountRole targetRole,
        Long targetId
) {

    public static MemberPermissionCheckCommand of(
            CurrentAccountDTO currentAccount,
            AccountRole targetRole,
            Long targetId
    ) {
        return new MemberPermissionCheckCommand(currentAccount, targetRole, targetId);
    }
}
