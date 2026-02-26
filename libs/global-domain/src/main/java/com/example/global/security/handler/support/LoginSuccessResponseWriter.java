package com.example.global.security.handler.support;

import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.global.security.SecurityHeaders;
import com.example.global.security.support.LocalTokenHeaderLoggingSupport;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginSuccessResponseWriter {

    private final LocalTokenHeaderLoggingSupport localTokenHeaderLoggingSupport;

    public void writeSuccess(HttpServletResponse response, LoginTokenResponse loginTokenResponse) {
        if (response == null || response.isCommitted() || loginTokenResponse == null) {
            return;
        }

        localTokenHeaderLoggingSupport.logResponseTokenHeaders(
                "login",
                loginTokenResponse.accessToken(),
                loginTokenResponse.refreshToken()
        );
        response.setHeader(
                SecurityHeaders.AUTHORIZATION,
                SecurityHeaders.BEARER_PREFIX + loginTokenResponse.accessToken()
        );
        response.setHeader(SecurityHeaders.REFRESH_TOKEN, loginTokenResponse.refreshToken());
        response.setStatus(HttpStatus.NO_CONTENT.value());
    }
}
