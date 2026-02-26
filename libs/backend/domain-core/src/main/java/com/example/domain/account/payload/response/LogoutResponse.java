package com.example.domain.account.payload.response;

/**
 * 로그아웃 응답 DTO
 */
public record LogoutResponse(
        String message
) {

    public static LogoutResponse of(String message) {
        return new LogoutResponse(message);
    }
}
