package com.example.global.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * 날짜/시간 포맷 전용 유틸
 * <p>
 * - 단일 책임 원칙에 따라 날짜/시간 포맷 책임만 담당합니다.
 */
public final class DateTimeFormatUtils {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KOREAN_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");
    private static final DateTimeFormatter KOREAN_DATE_TIME_WITH_MINUTES_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분");

    private DateTimeFormatUtils() {
    }

    public static String formatKoreanDateTime(final LocalDateTime localDateTime) {
        Objects.requireNonNull(localDateTime, "localDateTime은 필수입니다.");
        return localDateTime.format(KOREAN_DATE_TIME_FORMATTER);
    }

    public static String nowKoreanDateTime() {
        return LocalDateTime.now(KOREA_ZONE).format(KOREAN_DATE_TIME_WITH_MINUTES_FORMATTER);
    }
}
