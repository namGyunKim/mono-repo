package com.example.domain.member.payload.dto;

import com.example.global.security.payload.SecurityLogoutCommand;

/**
 * 회원 비활성화(탈퇴) 요청 DTO
 */
public record MemberDeactivateCommand(
        Long memberId,
        Long currentAccountId,
        SecurityLogoutCommand logoutCommand
) {

    public static MemberDeactivateCommand of(Long memberId) {
        return of(memberId, null, null);
    }

    public static MemberDeactivateCommand of(Long memberId, Long currentAccountId) {
        return of(memberId, currentAccountId, null);
    }

    public static MemberDeactivateCommand of(Long memberId, Long currentAccountId, SecurityLogoutCommand logoutCommand) {
        return new MemberDeactivateCommand(memberId, currentAccountId, logoutCommand);
    }
}
