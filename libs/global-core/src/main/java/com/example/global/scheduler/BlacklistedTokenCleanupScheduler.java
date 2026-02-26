package com.example.global.scheduler;

import com.example.global.security.blacklist.service.command.BlacklistedTokenCleanupCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BlacklistedTokenCleanupScheduler {

    private final BlacklistedTokenCleanupCommandService blacklistedTokenCleanupCommandService;

    @Scheduled(cron = "${app.jwt.blacklist-cleanup-cron:0 0 3 * * *}")
    public void cleanup() {
        blacklistedTokenCleanupCommandService.cleanupExpiredTokens();
    }
}
