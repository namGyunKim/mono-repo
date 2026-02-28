package com.example.global.security.jwt;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenKeyProvider {

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtTokenKeyProvider(final JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public JwtProperties getProperties() {
        return properties;
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }
}
