package com.example.domain.account.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;

/**
 * 현재 로그인 사용자 DTO
 *
 * <p>
 * - GEMINI 규칙: DTO는 record 사용
 * - 외부에서는 new 생성자 호출 대신 정적 팩토리 메서드(of/...)만 사용합니다.
 * </p>
 */
public record CurrentAccountDTO(
        Long id,                 // 회원 아이디
        String loginId,          // 로그인 아이디
        String nickName,         // 닉네임
        AccountRole role,        // 권한
        MemberType memberType    // 회원 타입
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

    public static CurrentAccountDTO generatedGuest() {
        return of(0L, "GUEST", "GUEST", AccountRole.GUEST, MemberType.GENERAL);
    }
}
