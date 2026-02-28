package com.example.global.security.jwt;

import com.example.global.security.SecurityHeaders;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class AccessTokenResolver {

    public Optional<String> resolveAccessToken(final HttpServletRequest request) {
        if (request == null) {
            return Optional.empty();
        }

        final String header = request.getHeader(SecurityHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(header) || !StringUtils.startsWithIgnoreCase(header, SecurityHeaders.BEARER_PREFIX)) {
            return Optional.empty();
        }

        final String token = header.substring(SecurityHeaders.BEARER_PREFIX.length()).trim();
        return StringUtils.hasText(token) ? Optional.of(token) : Optional.empty();
    }
}
