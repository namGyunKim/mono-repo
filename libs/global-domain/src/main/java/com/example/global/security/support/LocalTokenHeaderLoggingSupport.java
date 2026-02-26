package com.example.global.security.support;

import com.example.global.security.SecurityHeaders;
import com.example.global.utils.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalTokenHeaderLoggingSupport {

    private final Environment environment;

    public void logResponseTokenHeaders(String flow, String accessToken, String refreshToken) {
        if (!isLocalProfileActive()) {
            return;
        }

        final String authorizationHeaderValue = SecurityHeaders.BEARER_PREFIX + String.valueOf(accessToken);
        final String refreshHeaderValue = String.valueOf(refreshToken);
        log.info(
                """
                        traceId={}, flow={}, local 프로필 토큰 헤더 로그
                        {}={}
                        {}={}
                        """.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                flow,
                SecurityHeaders.AUTHORIZATION,
                authorizationHeaderValue,
                SecurityHeaders.REFRESH_TOKEN,
                refreshHeaderValue
        );
    }

    private boolean isLocalProfileActive() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("local"));
    }
}
