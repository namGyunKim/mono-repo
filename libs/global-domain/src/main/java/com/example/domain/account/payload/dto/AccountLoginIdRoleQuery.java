package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;

/**
 * 로그인 아이디/권한 조합 조회용 DTO
 * <p>
 * - GEMINI 규칙: DTO는 record + 정적 팩토리 메서드(of)를 제공합니다.
 */
public record AccountLoginIdRoleQuery(
        String loginId,
        AccountRole role
) {

    public static AccountLoginIdRoleQuery of(String loginId, AccountRole role) {
        return new AccountLoginIdRoleQuery(loginId, role);
    }
}
