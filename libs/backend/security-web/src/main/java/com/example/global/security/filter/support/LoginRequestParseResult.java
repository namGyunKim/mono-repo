package com.example.global.security.filter.support;

import com.example.global.payload.response.ApiErrorDetail;

import java.util.List;

public record LoginRequestParseResult(
        Object loginRequest,
        List<ApiErrorDetail> errors
) {
    public static LoginRequestParseResult success(final Object loginRequest) {
        return new LoginRequestParseResult(loginRequest, List.of());
    }

    public static LoginRequestParseResult of(final Object loginRequest) {
        return success(loginRequest);
    }

    public static LoginRequestParseResult failure(final List<ApiErrorDetail> errors) {
        final List<ApiErrorDetail> safeErrors = (errors == null) ? List.of() : List.copyOf(errors);
        return new LoginRequestParseResult(null, safeErrors);
    }

    public static LoginRequestParseResult from(final List<ApiErrorDetail> errors) {
        return failure(errors);
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
