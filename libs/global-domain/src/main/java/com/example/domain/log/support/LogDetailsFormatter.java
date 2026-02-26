package com.example.domain.log.support;

import com.example.domain.log.enums.LogType;
import org.springframework.util.StringUtils;

/**
 * 로그 상세(details) 메시지 포맷터
 * <p>
 * [목적]
 * - 서비스/핸들러마다 제각각인 details 문구를 하나의 규칙으로 통일합니다.
 * - 검색/필터링 관점에서 '행위/대상/내용' 키-값 형태를 유지하여 로그 가독성을 높입니다.
 */
public final class LogDetailsFormatter {

    private static final int MAX_DETAILS_LENGTH = 500;
    private static final String TRUNCATE_SUFFIX = "...";

    private LogDetailsFormatter() {
    }

    public static String format(LogType logType, String target, String message) {
        String action = resolveAction(logType);
        String safeTarget = StringUtils.hasText(target) ? target : "UNKNOWN";
        String safeMessage = (message == null) ? "" : message.trim();

        if (safeMessage.isBlank()) {
            return truncateIfNeeded("행위=%s | 대상=%s".formatted(action, safeTarget));
        }

        return truncateIfNeeded("행위=%s | 대상=%s | 내용=%s".formatted(action, safeTarget, safeMessage));
    }

    private static String resolveAction(LogType logType) {
        if (logType == null) {
            return "UNKNOWN";
        }

        String description = logType.getDescription();
        if (!StringUtils.hasText(description)) {
            return logType.name();
        }

        return "%s(%s)".formatted(description, logType.name());
    }

    private static String truncateIfNeeded(String value) {
        if (value == null || value.length() <= MAX_DETAILS_LENGTH) {
            return value;
        }
        int maxPrefixLength = MAX_DETAILS_LENGTH - TRUNCATE_SUFFIX.length();
        if (maxPrefixLength <= 0) {
            return value.substring(0, MAX_DETAILS_LENGTH);
        }
        return value.substring(0, maxPrefixLength) + TRUNCATE_SUFFIX;
    }
}
