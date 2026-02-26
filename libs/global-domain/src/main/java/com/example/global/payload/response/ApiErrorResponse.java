package com.example.global.payload.response;

import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.TraceIdUtils;

import java.util.List;

/**
 * 공통 에러 응답 DTO
 *
 * <p>
 * - GEMINI 규칙: DTO는 record 사용
 * - 외부에서 new 호출을 금지하기 위해 정적 팩토리 메서드를 제공합니다.
 * </p>
 */
public record ApiErrorResponse(
        String code,
        String message,
        String requestId,
        List<ApiErrorDetail> errors
) {

    public ApiErrorResponse {
        if (code == null) {
            code = "";
        }
        if (message == null) {
            message = "";
        }
        if (requestId == null || requestId.isBlank()) {
            requestId = TraceIdUtils.resolveTraceId();
        }
        if (errors == null) {
            errors = List.of();
        } else {
            errors = List.copyOf(errors);
        }
    }

    public static ApiErrorResponse of(String code, String message, String requestId, List<ApiErrorDetail> errors) {
        return new ApiErrorResponse(code, message, requestId, errors);
    }

    public static ApiErrorResponse from(ErrorCode errorCode) {
        return from(errorCode, List.of());
    }

    public static ApiErrorResponse from(ErrorCode errorCode, List<ApiErrorDetail> errors) {
        if (errorCode == null) {
            return of("", "", TraceIdUtils.resolveTraceId(), errors);
        }
        return of(
                errorCode.getCode(),
                errorCode.getErrorMessage(),
                TraceIdUtils.resolveTraceId(),
                errors
        );
    }
}
