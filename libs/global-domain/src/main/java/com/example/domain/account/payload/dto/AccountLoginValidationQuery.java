package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;

import java.util.List;

/**
 * 로그인 정책 검증 조회 요청 DTO
 */
public record AccountLoginValidationQuery(
        String loginId,
        List<AccountRole> allowedRoles
) {
    public static AccountLoginValidationQuery of(String loginId, List<AccountRole> allowedRoles) {
        List<AccountRole> safeAllowedRoles = allowedRoles == null ? List.of() : List.copyOf(allowedRoles);
        return new AccountLoginValidationQuery(loginId, safeAllowedRoles);
    }
}
