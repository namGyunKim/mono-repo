package com.example.global.security.blacklist.payload.dto;

import java.time.LocalDateTime;

/**
 * 블랙리스트 만료 토큰 정리용 Command DTO
 */
public record BlacklistedTokenCleanupCommand(
        LocalDateTime expiresAt
) {

    public static BlacklistedTokenCleanupCommand of(final LocalDateTime expiresAt) {
        if (expiresAt == null) {
            throw new IllegalArgumentException("expiresAt은 필수입니다.");
        }
        return new BlacklistedTokenCleanupCommand(expiresAt);
    }
}
