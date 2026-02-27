package com.example.domain.member.payload.request;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.contract.enums.ApiAccountRole;
import jakarta.validation.constraints.NotNull;

public record MemberRoleUpdateRequest(
        @NotNull(message = "변경할 권한을 선택해주세요.")
        ApiAccountRole role
) {

    public static MemberRoleUpdateRequest of(ApiAccountRole role) {
        return new MemberRoleUpdateRequest(role);
    }

    public AccountRole toDomainRole() {
        return role != null ? role.toDomain() : null;
    }
}
