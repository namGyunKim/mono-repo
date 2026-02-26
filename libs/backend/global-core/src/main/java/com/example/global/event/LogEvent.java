package com.example.global.event;

/**
 * 단순 로그 이벤트 객체
 *
 * <p>
 * - 베이스 프로젝트에서는 이벤트 기반 로깅을 지원합니다.
 * - record로 유지하며, Builder 패턴은 사용하지 않습니다.
 *
 * <p>
 * [네이밍 규칙 표준화]
 * - of(...): 원시값/직접 값 기반 생성
 * </p>
 */

import com.example.global.utils.SensitiveLogMessageSanitizer;
import com.example.global.utils.TraceIdUtils;

public record LogEvent(
        String traceId,
        String message
) {

    public LogEvent {
        message = SensitiveLogMessageSanitizer.sanitize(message);
    }

    public static LogEvent of(String message) {
        return of(TraceIdUtils.resolveTraceId(), message);
    }

    public static LogEvent of(String traceId, String message) {
        String resolvedTraceId = (traceId == null || traceId.isBlank())
                ? TraceIdUtils.resolveTraceId()
                : traceId;
        return new LogEvent(resolvedTraceId, message);
    }

}
