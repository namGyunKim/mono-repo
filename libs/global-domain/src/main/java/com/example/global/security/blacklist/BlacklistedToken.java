package com.example.global.security.blacklist;

import com.example.global.entity.BaseTimeEntity;
import com.example.global.security.jwt.JwtTokenType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "blacklisted_token_id", columnDefinition = "uuid", comment = "블랙리스트 토큰 ID")
    private UUID id;

    @Column(name = "token_hash", length = 64, nullable = false, unique = true, comment = "폐기된 토큰 해시(SHA-256)")
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, comment = "토큰 타입")
    private JwtTokenType tokenType;

    @Column(nullable = false, comment = "토큰 만료 시각")
    private LocalDateTime expiresAt;

    @Column(length = 100, comment = "토큰 소유자")
    private String subject;

    public static BlacklistedToken of(String tokenHash, JwtTokenType tokenType, LocalDateTime expiresAt, String subject) {
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.tokenHash = tokenHash;
        blacklistedToken.tokenType = tokenType;
        blacklistedToken.expiresAt = expiresAt;
        blacklistedToken.subject = subject;
        return blacklistedToken;
    }
}
