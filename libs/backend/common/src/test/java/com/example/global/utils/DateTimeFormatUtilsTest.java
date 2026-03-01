package com.example.global.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateTimeFormatUtilsTest {

    @Test
    void formatKoreanDateTime_formats_correctly() {
        final LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 0);
        final String result = DateTimeFormatUtils.formatKoreanDateTime(dateTime);
        assertThat(result).isEqualTo("2024년 01월 15일 14:30");
    }

    @Test
    void formatKoreanDateTime_midnight_formats_with_zero_hours() {
        final LocalDateTime dateTime = LocalDateTime.of(2024, 12, 25, 0, 0, 0);
        final String result = DateTimeFormatUtils.formatKoreanDateTime(dateTime);
        assertThat(result).isEqualTo("2024년 12월 25일 00:00");
    }

    @Test
    void formatKoreanDateTime_null_throws_NullPointerException() {
        assertThatThrownBy(() -> DateTimeFormatUtils.formatKoreanDateTime(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void nowKoreanDateTime_returns_non_null_with_korean_pattern() {
        final String result = DateTimeFormatUtils.nowKoreanDateTime();
        assertThat(result).isNotNull();
        assertThat(result).contains("년", "월", "일", "시", "분");
    }
}
