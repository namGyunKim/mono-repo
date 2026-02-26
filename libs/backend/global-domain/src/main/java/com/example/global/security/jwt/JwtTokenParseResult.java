package com.example.global.security.jwt;

import java.util.Optional;

public record JwtTokenParseResult(
        JwtTokenParseStatus status,
        JwtTokenPayload payload
) {

    public JwtTokenParseResult {
        if (status == null) {
            status = JwtTokenParseStatus.INVALID;
        }
        if (status == JwtTokenParseStatus.VALID && payload == null) {
            status = JwtTokenParseStatus.INVALID;
        }
        if (status != JwtTokenParseStatus.VALID) {
            payload = null;
        }
    }

    public static JwtTokenParseResult of(JwtTokenParseStatus status, JwtTokenPayload payload) {
        return new JwtTokenParseResult(status, payload);
    }

    public Optional<JwtTokenPayload> payloadOptional() {
        return Optional.ofNullable(payload);
    }
}
