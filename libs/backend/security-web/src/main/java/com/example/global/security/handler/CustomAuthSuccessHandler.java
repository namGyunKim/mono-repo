package com.example.global.security.handler;

import com.example.domain.account.payload.response.LoginTokenResponse;
import com.example.domain.security.guard.PrincipalDetails;
import com.example.domain.security.token.LoginTokenCommandService;
import com.example.global.security.handler.support.LoginSuccessEventPublisher;
import com.example.global.security.handler.support.LoginSuccessMessageResolver;
import com.example.global.security.handler.support.LoginSuccessResponseWriter;
import com.example.global.security.payload.LoginTokenIssueCommand;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginTokenCommandService loginTokenCommandService;
    private final LoginSuccessEventPublisher loginSuccessEventPublisher;
    private final LoginSuccessMessageResolver loginSuccessMessageResolver;
    private final LoginSuccessResponseWriter loginSuccessResponseWriter;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        final PrincipalDetails principal = (PrincipalDetails) authentication.getPrincipal();

        final LoginTokenResponse loginTokenResponse = loginTokenCommandService.issueTokens(
                LoginTokenIssueCommand.of(principal.getId())
        );
        final String message = loginSuccessMessageResolver.resolve(request);
        loginSuccessEventPublisher.publish(principal, message);
        loginSuccessResponseWriter.writeSuccess(response, loginTokenResponse);
    }
}
