package com.example.global.security.payload;

public record SecurityLogoutCommand(
        Long memberId,
        String accessToken
) {
    public static SecurityLogoutCommand of(final Long memberId, final String accessToken) {
        return new SecurityLogoutCommand(memberId, accessToken);
    }
}
