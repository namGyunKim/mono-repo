package com.example.global.security.service.command;

import com.example.domain.member.entity.Member;
import com.example.global.security.jwt.JwtTokenClaimKeys;
import com.example.global.security.jwt.JwtTokenKeyProvider;
import com.example.global.security.jwt.JwtTokenType;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class JwtTokenCommandService {

    private final JwtTokenKeyProvider keyProvider;

    public String generateAccessToken(Member member) {
        return generateToken(member, JwtTokenType.ACCESS, keyProvider.getProperties().accessTokenTtl());
    }

    public String generateRefreshToken(Member member) {
        return generateToken(member, JwtTokenType.REFRESH, keyProvider.getProperties().refreshTokenTtl());
    }

    private String generateToken(Member member, JwtTokenType tokenType, java.time.Duration ttl) {
        validateTtl(ttl, tokenType.name().toLowerCase());
        Instant now = Instant.now();
        Instant expiresAt = now.plus(ttl);

        return Jwts.builder()
                .issuer(keyProvider.getProperties().issuer())
                .subject(member.getLoginId())
                .claim(JwtTokenClaimKeys.ROLE, member.getRole().name())
                .claim(JwtTokenClaimKeys.TYPE, tokenType.name())
                .claim(JwtTokenClaimKeys.VERSION, member.getTokenVersion())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(keyProvider.getSecretKey())
                .compact();
    }

    private void validateTtl(java.time.Duration ttl, String tokenLabel) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("JWT " + tokenLabel + " TTL은 0보다 커야 합니다.");
        }
    }
}
