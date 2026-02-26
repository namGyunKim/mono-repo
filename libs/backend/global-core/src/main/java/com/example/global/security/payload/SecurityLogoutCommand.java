package com.example.global.security.payload;

public record SecurityLogoutCommand(
        Long memberId,
        String accessToken
) {
    public static SecurityLogoutCommand of(Long memberId, String accessToken) {
        return new SecurityLogoutCommand(memberId, accessToken);
    }
}
