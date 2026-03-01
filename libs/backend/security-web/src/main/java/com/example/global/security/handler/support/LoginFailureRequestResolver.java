package com.example.global.security.handler.support;

import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.security.filter.JsonBodyLoginAuthenticationFilter;
import com.example.global.utils.LoginLoggingUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class LoginFailureRequestResolver {

    private static final String PARAM_LOGIN_ID = "loginId";
    private static final String PARAM_PASSWORD = "password";

    public List<ApiErrorDetail> resolveMissingCredentialErrors(HttpServletRequest request) {
        if (isJsonRequest(request)) {
            return List.of();
        }

        final String loginId = request != null ? request.getParameter(PARAM_LOGIN_ID) : null;
        final String password = request != null ? request.getParameter(PARAM_PASSWORD) : null;

        final List<ApiErrorDetail> errors = new ArrayList<>();
        if (loginId == null || loginId.isBlank()) {
            errors.add(ApiErrorDetail.of(PARAM_LOGIN_ID, "로그인 아이디를 입력해주세요."));
        }
        if (password == null || password.isBlank()) {
            errors.add(ApiErrorDetail.of(PARAM_PASSWORD, "비밀번호를 입력해주세요."));
        }
        return errors;
    }

    public Optional<String> resolveLoginId(HttpServletRequest request) {
        return LoginLoggingUtils.extractLoginId(
                request,
                PARAM_LOGIN_ID,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID
        );
    }

    public String resolveLoginIdOrDefault(HttpServletRequest request, String defaultValue) {
        return LoginLoggingUtils.resolveLoginIdOrDefault(
                request,
                PARAM_LOGIN_ID,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID,
                defaultValue
        );
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        final String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase(Locale.ROOT).contains(MediaType.APPLICATION_JSON_VALUE);
    }
}
