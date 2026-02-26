package com.example.domain.security.jwt;

import com.example.domain.account.enums.AccountRole;
import com.example.global.security.jwt.JwtTokenClaimKeys;
import com.example.global.security.jwt.JwtTokenKeyProvider;
import com.example.global.security.jwt.JwtTokenParseStatus;
import com.example.global.security.jwt.JwtTokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtTokenParser {

    private final JwtTokenKeyProvider keyProvider;

    public Optional<JwtTokenPayload> parseToken(String token) {
        JwtTokenParseResult result = parseTokenResult(token);
        if (result.status() != JwtTokenParseStatus.VALID) {
            return Optional.empty();
        }
        return result.payloadOptional();
    }

    public JwtTokenParseResult parseTokenResult(String token) {
        if (!StringUtils.hasText(token)) {
            return JwtTokenParseResult.of(JwtTokenParseStatus.INVALID, null);
        }

        try {
            Jws<Claims> parsed = Jwts.parser()
                    .verifyWith(keyProvider.getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            Claims claims = parsed.getPayload();
            String subject = claims.getSubject();
            String issuer = claims.getIssuer();
            String tokenId = claims.getId();
            String roleValue = claims.get(JwtTokenClaimKeys.ROLE, String.class);
            String typeValue = claims.get(JwtTokenClaimKeys.TYPE, String.class);
            Number versionValue = claims.get(JwtTokenClaimKeys.VERSION, Number.class);
            Instant issuedAt = Optional.ofNullable(claims.getIssuedAt())
                    .map(Date::toInstant)
                    .orElse(null);
            Instant expiresAt = Optional.ofNullable(claims.getExpiration())
                    .map(Date::toInstant)
                    .orElse(null);
            Instant now = Instant.now();

            if (expiresAt != null && !expiresAt.isAfter(now)) {
                return JwtTokenParseResult.of(JwtTokenParseStatus.EXPIRED, null);
            }

            if (!StringUtils.hasText(subject)
                    || !StringUtils.hasText(issuer)
                    || !StringUtils.hasText(tokenId)
                    || !StringUtils.hasText(roleValue)
                    || !StringUtils.hasText(typeValue)
                    || !keyProvider.getProperties().issuer().equals(issuer)
                    || issuedAt == null
                    || expiresAt == null
                    || versionValue == null
                    || !expiresAt.isAfter(issuedAt)
                    || issuedAt.isAfter(now)) {
                return JwtTokenParseResult.of(JwtTokenParseStatus.INVALID, null);
            }

            AccountRole role = AccountRole.valueOf(roleValue);
            JwtTokenType tokenType = JwtTokenType.valueOf(typeValue);
            long tokenVersion = versionValue.longValue();
            if (tokenVersion < 0L) {
                return JwtTokenParseResult.of(JwtTokenParseStatus.INVALID, null);
            }
            JwtTokenPayload payload = JwtTokenPayload.of(subject, role, tokenType, tokenVersion, issuedAt, expiresAt);
            return JwtTokenParseResult.of(JwtTokenParseStatus.VALID, payload);
        } catch (ExpiredJwtException ex) {
            return JwtTokenParseResult.of(JwtTokenParseStatus.EXPIRED, null);
        } catch (Exception ex) {
            return JwtTokenParseResult.of(JwtTokenParseStatus.INVALID, null);
        }
    }
}
