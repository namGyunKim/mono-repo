package com.example.global.security.jwt;

import com.example.domain.account.enums.AccountRole;

import java.time.Instant;

public record JwtTokenPayload(
        String subject,
        AccountRole role,
        JwtTokenType tokenType,
        long tokenVersion,
        Instant issuedAt,
        Instant expiresAt
) {

    public static JwtTokenPayload of(
            String subject,
            AccountRole role,
            JwtTokenType tokenType,
            long tokenVersion,
            Instant issuedAt,
            Instant expiresAt
    ) {
        return new JwtTokenPayload(subject, role, tokenType, tokenVersion, issuedAt, expiresAt);
    }
}
