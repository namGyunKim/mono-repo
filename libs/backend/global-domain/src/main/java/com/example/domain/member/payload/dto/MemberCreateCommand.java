package com.example.domain.member.payload.dto;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberType;
import com.example.domain.member.payload.request.MemberCreateRequest;

/**
 * 서비스 계층으로 전달되는 회원 생성 명령 DTO
 * <p>
 * - Controller -> Service 경계에서 파라미터 나열을 방지하기 위한 전용 DTO입니다.
 */
public record MemberCreateCommand(
        String loginId,
        String nickName,
        String password,
        AccountRole role,
        MemberType memberType
) {

    public static MemberCreateCommand of(
            String loginId,
            String nickName,
            String password,
            AccountRole role,
            MemberType memberType
    ) {
        return new MemberCreateCommand(loginId, nickName, password, role, memberType);
    }

    public static MemberCreateCommand from(MemberCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return of(request.loginId(), request.nickName(), request.password(), request.role(), request.memberType());
    }

    public MemberCreateCommand withRole(AccountRole role) {
        return of(loginId(), nickName(), password(), role, memberType());
    }
}
