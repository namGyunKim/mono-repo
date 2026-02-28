package com.example.domain.member.payload.dto;

/**
 * 로그인 ID 조회 요청 DTO
 */
public record MemberLoginIdQuery(String loginId) {

    public static MemberLoginIdQuery of(String loginId) {
        return new MemberLoginIdQuery(loginId);
    }
}
