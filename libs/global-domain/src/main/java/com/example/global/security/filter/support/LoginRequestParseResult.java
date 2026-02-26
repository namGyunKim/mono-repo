package com.example.global.security.filter.support;

import com.example.global.payload.response.ApiErrorDetail;

import java.util.List;

public record LoginRequestParseResult(
        Object loginRequest,
        List<ApiErrorDetail> errors
) {
    public static LoginRequestParseResult success(Object loginRequest) {
        return new LoginRequestParseResult(loginRequest, List.of());
    }

    public static LoginRequestParseResult of(Object loginRequest) {
        return success(loginRequest);
    }

    public static LoginRequestParseResult failure(List<ApiErrorDetail> errors) {
        List<ApiErrorDetail> safeErrors = (errors == null) ? List.of() : List.copyOf(errors);
        return new LoginRequestParseResult(null, safeErrors);
    }

    public static LoginRequestParseResult from(List<ApiErrorDetail> errors) {
        return failure(errors);
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
