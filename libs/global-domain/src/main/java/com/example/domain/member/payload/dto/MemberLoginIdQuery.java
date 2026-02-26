package com.example.domain.member.payload.dto;

/**
 * 로그인 ID 조회 요청 DTO
 * <p>
 * - GEMINI 규칙: DTO는 record + 정적 팩토리 메서드(of)를 제공합니다.
 * </p>
 */
public record MemberLoginIdQuery(String loginId) {

    public static MemberLoginIdQuery of(String loginId) {
        return new MemberLoginIdQuery(loginId);
    }
}
