package com.example.domain.member.payload.dto;

/**
 * 로그인 아이디 중복 체크용 조회 DTO
 */
public record MemberLoginIdDuplicateCheckQuery(String loginId) {

    public static MemberLoginIdDuplicateCheckQuery of(String loginId) {
        return new MemberLoginIdDuplicateCheckQuery(loginId);
    }
}
