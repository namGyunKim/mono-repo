package com.example.global.security.blacklist.service.command;

import com.example.global.security.blacklist.BlacklistedTokenRepository;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenCleanupCommand;
import com.example.global.security.blacklist.support.BlacklistedTokenCleanupLogWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenCleanupCommandService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final BlacklistedTokenCleanupLogWriter cleanupLogWriter;

    @Transactional
    public void cleanupExpiredTokens() {
        long startedAt = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        long deletedCount = blacklistedTokenRepository.deleteExpiredTokens(BlacklistedTokenCleanupCommand.of(now));
        long elapsedMs = Math.max(0, System.currentTimeMillis() - startedAt);

        cleanupLogWriter.logCleanup(now, deletedCount, elapsedMs);
    }
}
