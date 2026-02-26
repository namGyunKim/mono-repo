package com.example.domain.account.payload.dto;

/**
 * 로그인 ID 조회 요청 DTO
 * <p>
 * GEMINI 규칙: DTO는 record + 정적 팩토리 메서드(of)를 제공합니다.
 * - 외부에서 new AccountLoginIdQuery(...) 호출을 금지하기 위한 표준 생성 메서드입니다.
 * </p>
 */
public record AccountLoginIdQuery(String loginId) {

    public static AccountLoginIdQuery of(String loginId) {
        return new AccountLoginIdQuery(loginId);
    }
}
