package com.example.domain.member.payload.dto;

import com.example.domain.member.payload.request.MemberUpdateRequest;

/**
 * 서비스 계층으로 전달되는 회원 정보 수정 명령 DTO
 * <p>
 * - Controller -> Service 경계에서 파라미터 나열을 방지하기 위한 전용 DTO입니다.
 */
public record MemberUpdateCommand(
        String nickName,
        String password,
        Long memberId
) {

    public static MemberUpdateCommand of(String nickName, String password, Long memberId) {
        return new MemberUpdateCommand(nickName, password, memberId);
    }

    public static MemberUpdateCommand from(MemberUpdateRequest request, Long memberId) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return of(request.nickName(), request.password(), memberId);
    }
}