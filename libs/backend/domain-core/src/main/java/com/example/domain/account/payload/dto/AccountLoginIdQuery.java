package com.example.domain.account.payload.dto;

/**
 * 로그인 ID 조회 요청 DTO
 */
public record AccountLoginIdQuery(String loginId) {

    public static AccountLoginIdQuery of(String loginId) {
        return new AccountLoginIdQuery(loginId);
    }
}
