package com.example.global.utils;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Enum 파싱 공통 유틸
 * - name() 기준, 대소문자 무시, trim 처리
 * - 값이 없거나 매칭 실패 시 null 반환
 */
public final class EnumParser {

    private EnumParser() {
    }

    /**
     * Enum의 name()을 Key로 하는 Map을 생성합니다.
     * - Key는 대소문자 무시 비교를 위해 Locale.ROOT 기준 대문자로 정규화하여 저장합니다.
     * - null/빈 배열이면 빈 Map을 반환합니다.
     */
    public static <E extends Enum<E>> Map<String, E> toNameMap(final E[] values) {
        if (values == null || values.length == 0) {
            return Map.of();
        }

        return Arrays.stream(values)
                .collect(Collectors.toUnmodifiableMap(
                        v -> v.name().toUpperCase(Locale.ROOT),
                        Function.identity(),
                        // Enum name()은 중복될 수 없지만, 방어적으로 첫 값 유지
                        (a, b) -> a
                ));
    }

    /**
     * toNameMap(...)으로 만들어진 Map 기반으로 name()을 대소문자 무시(Trim 포함)하여 파싱합니다.
     * - raw가 null/blank이거나 매칭 실패 시 defaultValue를 반환합니다.
     */
    public static <E extends Enum<E>> E fromNameIgnoreCase(final Map<String, E> nameMap, final String raw, final E defaultValue) {
        if (nameMap == null || raw == null) {
            return defaultValue;
        }

        final String normalized = raw.trim();
        if (normalized.isEmpty()) {
            return defaultValue;
        }

        final E found = nameMap.get(normalized.toUpperCase(Locale.ROOT));
        return (found != null) ? found : defaultValue;
    }

    public static <E extends Enum<E>> E fromNameIgnoreCase(final Class<E> enumType, final String raw) {
        if (enumType == null || raw == null) {
            return null;
        }

        final String normalized = raw.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        final E[] constants = enumType.getEnumConstants();
        if (constants == null) {
            return null;
        }

        return Stream.of(constants)
                .filter(v -> v.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }
}
