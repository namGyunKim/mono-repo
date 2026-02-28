package com.example.global.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Optional;

/**
 * Spring MVC PathVariable 추출 유틸
 *
 * <p>
 * - Validator(@InitBinder)에서 PathVariable 값이 필요할 때, 컨트롤러 본문으로 검증 로직이 새어 나가지 않도록
 * HttpServletRequest의 URI_TEMPLATE_VARIABLES_ATTRIBUTE를 통해 안전하게 값을 추출합니다.
 * - 값이 없거나 파싱 실패 시 Optional.empty()를 반환합니다.
 * </p>
 */
public final class RequestPathVariableUtils {

    private RequestPathVariableUtils() {
    }

    public static Optional<String> findPathVariable(HttpServletRequest request, String name) {
        if (request == null || name == null || name.isBlank()) {
            return Optional.empty();
        }

        final Object attribute = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attribute instanceof Map<?, ?> map)) {
            return Optional.empty();
        }

        final Object value = map.get(name);
        if (value == null) {
            return Optional.empty();
        }

        final String stringValue = String.valueOf(value).trim();
        if (stringValue.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(stringValue);
    }

    public static Optional<Long> findPathVariableAsLong(HttpServletRequest request, String name) {
        return findPathVariable(request, name).flatMap(raw -> {
            try {
                return Optional.of(Long.parseLong(raw));
            } catch (NumberFormatException e) {
                return Optional.empty();
            }
        });
    }
}
