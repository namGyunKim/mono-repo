package com.example.global.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 자유 형식 문자열 로그에 포함될 수 있는 민감정보를 마스킹하는 유틸리티
 */
public final class SensitiveLogMessageSanitizer {

    private static final String MASKED_VALUE = "***";

    private static final Pattern JSON_PAIR_PATTERN =
            Pattern.compile("\"([A-Za-z][A-Za-z0-9_-]*)\"\\s*:\\s*\"([^\"]*)\"");

    private static final Pattern KEY_VALUE_PATTERN =
            Pattern.compile("\\b([A-Za-z][A-Za-z0-9_-]*)\\b\\s*([=:])\\s*([^\\s,|;]+)");

    private static final Pattern BEARER_PATTERN =
            Pattern.compile("(?i)(Bearer\\s+)([A-Za-z0-9._~+\\-/]+=*)");

    private SensitiveLogMessageSanitizer() {
    }

    public static String sanitize(String message) {
        if (message == null || message.isBlank()) {
            return message;
        }

        String masked = maskJsonPairs(message);
        masked = maskKeyValuePairs(masked);
        return maskBearerToken(masked);
    }

    private static String maskJsonPairs(String message) {
        Matcher matcher = JSON_PAIR_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (!LoggingSanitizerPolicy.isSensitiveField(fieldName)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            String replacement = "\"%s\":\"%s\"".formatted(fieldName, MASKED_VALUE);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String maskKeyValuePairs(String message) {
        Matcher matcher = KEY_VALUE_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String fieldName = matcher.group(1);
            if (!LoggingSanitizerPolicy.isSensitiveField(fieldName)) {
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group(0)));
                continue;
            }

            String replacement = "%s%s%s".formatted(fieldName, matcher.group(2), MASKED_VALUE);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String maskBearerToken(String message) {
        Matcher matcher = BEARER_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String replacement = "%s%s".formatted(matcher.group(1), MASKED_VALUE);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
