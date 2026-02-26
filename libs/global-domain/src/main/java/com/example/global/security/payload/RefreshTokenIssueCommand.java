package com.example.global.security.payload;

/**
 * 리프레시 토큰 갱신 요청 DTO
 */
public record RefreshTokenIssueCommand(
        String refreshToken
) {

    public static RefreshTokenIssueCommand of(String refreshToken) {
        return new RefreshTokenIssueCommand(refreshToken);
    }
}
