package com.example.global.security.blacklist.support;

import com.example.global.security.TokenHashUtils;
import com.example.global.security.blacklist.BlacklistedTokenRepository;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenHashQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class BlacklistedTokenChecker {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public boolean isBlacklisted(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }

        final String tokenHash = TokenHashUtils.sha256(token);
        return blacklistedTokenRepository.existsByTokenHash(BlacklistedTokenHashQuery.of(tokenHash));
    }
}
