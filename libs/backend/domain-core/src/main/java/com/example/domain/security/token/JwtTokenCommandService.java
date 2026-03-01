package com.example.domain.security.token;

import com.example.domain.security.port.SecurityMemberTokenInfo;
import com.example.global.security.jwt.JwtTokenClaimKeys;
import com.example.global.security.jwt.JwtTokenKeyProvider;
import com.example.global.security.jwt.JwtTokenType;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenCommandService {

    private final JwtTokenKeyProvider keyProvider;

    public String generateAccessToken(SecurityMemberTokenInfo memberInfo) {
        return generateToken(memberInfo, JwtTokenType.ACCESS, keyProvider.getProperties().accessTokenTtl());
    }

    public String generateRefreshToken(SecurityMemberTokenInfo memberInfo) {
        return generateToken(memberInfo, JwtTokenType.REFRESH, keyProvider.getProperties().refreshTokenTtl());
    }

    private String generateToken(SecurityMemberTokenInfo memberInfo, JwtTokenType tokenType, Duration ttl) {
        validateTtl(ttl, tokenType.name().toLowerCase());
        final Instant now = Instant.now();
        final Instant expiresAt = now.plus(ttl);

        return Jwts.builder()
                .issuer(keyProvider.getProperties().issuer())
                .subject(memberInfo.loginId())
                .claim(JwtTokenClaimKeys.ROLE, memberInfo.role().name())
                .claim(JwtTokenClaimKeys.TYPE, tokenType.name())
                .claim(JwtTokenClaimKeys.VERSION, memberInfo.tokenVersion())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(keyProvider.getSecretKey())
                .compact();
    }

    private void validateTtl(Duration ttl, String tokenLabel) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("JWT %s TTL은 0보다 커야 합니다.".formatted(tokenLabel));
        }
    }
}
