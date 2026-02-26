package com.example.domain.member.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.request.MemberRoleUpdateRequest;

/**
 * 서비스 계층으로 전달되는 회원 권한 변경 명령 DTO
 * <p>
 * - Controller -> Service 경계에서 파라미터 나열을 방지하기 위한 전용 DTO입니다.
 */
public record MemberRoleUpdateCommand(
        Long memberId,
        AccountRole role
) {

    public static MemberRoleUpdateCommand of(Long memberId, AccountRole role) {
        return new MemberRoleUpdateCommand(memberId, role);
    }

    public static MemberRoleUpdateCommand from(Long memberId, MemberRoleUpdateRequest request) {
        AccountRole role = request != null ? request.role() : null;
        return of(memberId, role);
    }
}
