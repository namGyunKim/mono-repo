package com.example.domain.security.token;

import com.example.domain.security.jwt.JwtTokenParser;
import com.example.domain.security.jwt.JwtTokenPayload;
import com.example.global.utils.TokenHashUtils;
import com.example.global.security.blacklist.BlacklistedToken;
import com.example.global.security.blacklist.BlacklistedTokenRepository;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.blacklist.support.BlacklistedTokenChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BlacklistedTokenCommandService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JwtTokenParser jwtTokenParser;
    private final BlacklistedTokenChecker blacklistedTokenChecker;

    public void blacklistToken(BlacklistedTokenRegisterCommand command) {
        if (command == null || !StringUtils.hasText(command.token())) {
            return;
        }

        String token = command.token();
        if (blacklistedTokenChecker.isBlacklisted(token)) {
            return;
        }

        String tokenHash = TokenHashUtils.sha256(token);
        Optional<JwtTokenPayload> payload = jwtTokenParser.parseToken(token);
        if (payload.isEmpty()) {
            return;
        }

        JwtTokenPayload tokenPayload = payload.get();
        LocalDateTime expiresAt = LocalDateTime.ofInstant(tokenPayload.expiresAt(), ZoneId.systemDefault());
        BlacklistedToken entity = BlacklistedToken.of(
                tokenHash,
                tokenPayload.tokenType(),
                expiresAt,
                tokenPayload.subject()
        );
        blacklistedTokenRepository.save(entity);
    }
}
