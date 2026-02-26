package com.example.global.security.blacklist.support;

import com.example.global.utils.TraceIdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BlacklistedTokenCleanupLogWriter {

    public void logCleanup(LocalDateTime cutoffAt, long deletedCount, long elapsedMs) {
        log.info(
                """
                        [BLACKLIST_CLEANUP]
                        traceId={}
                        target={}
                        cutoffAt={}
                        deletedCount={}
                        elapsedMs={}
                        result={}
                        """.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                "BLACKLISTED_TOKEN",
                cutoffAt,
                deletedCount,
                elapsedMs,
                "SUCCESS"
        );
    }
}
