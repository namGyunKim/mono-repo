package com.example.global.security.jwt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank @Size(min = 32) String secret,
        @NotNull Duration accessTokenTtl,
        @NotNull Duration refreshTokenTtl
) {
    public static JwtProperties of(
            String issuer,
            String secret,
            Duration accessTokenTtl,
            Duration refreshTokenTtl
    ) {
        return new JwtProperties(issuer, secret, accessTokenTtl, refreshTokenTtl);
    }
}
