package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;

/**
 * 현재 로그인 사용자 DTO
 */
public record CurrentAccountDTO(
        Long id,
        String loginId,
        String nickName,
        AccountRole role,
        MemberType memberType
) {

    public static CurrentAccountDTO of(Long id, String loginId, String nickName, AccountRole role, MemberType memberType) {
        if (id == null) {
            throw new IllegalArgumentException("id는 필수입니다.");
        }
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException("loginId는 필수입니다.");
        }
        if (nickName == null || nickName.isBlank()) {
            throw new IllegalArgumentException("nickName은 필수입니다.");
        }
        if (role == null) {
            throw new IllegalArgumentException("role은 필수입니다.");
        }
        if (memberType == null) {
            throw new IllegalArgumentException("memberType은 필수입니다.");
        }
        return new CurrentAccountDTO(id, loginId, nickName, role, memberType);
    }

    public static CurrentAccountDTO ofGuest() {
        return of(0L, "GUEST", "GUEST", AccountRole.GUEST, MemberType.GENERAL);
    }
}
