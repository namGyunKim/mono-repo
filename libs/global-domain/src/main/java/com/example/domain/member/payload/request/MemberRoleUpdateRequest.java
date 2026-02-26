package com.example.domain.member.payload.request;

import com.example.domain.account.enums.AccountRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(
        @NotNull(message = "변경할 권한을 선택해주세요.")
        AccountRole role
) {

    public static MemberRoleUpdateRequest of(AccountRole role) {
        return new MemberRoleUpdateRequest(role);
    }
}
