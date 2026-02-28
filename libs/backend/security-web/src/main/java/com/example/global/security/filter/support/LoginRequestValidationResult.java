package com.example.global.security.filter.support;

import com.example.domain.account.validator.LoginRequestRoleStrategy;
import com.example.global.payload.response.ApiErrorDetail;

import java.util.List;

public record LoginRequestValidationResult(
        Object loginRequest,
        LoginRequestRoleStrategy strategy,
        String loginId,
        String password,
        List<ApiErrorDetail> errors
) {
    public static LoginRequestValidationResult of(
            Object loginRequest,
            LoginRequestRoleStrategy strategy,
            String loginId,
            String password,
            List<ApiErrorDetail> errors
    ) {
        final List<ApiErrorDetail> safeErrors = (errors == null) ? List.of() : List.copyOf(errors);
        return new LoginRequestValidationResult(loginRequest, strategy, loginId, password, safeErrors);
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}
