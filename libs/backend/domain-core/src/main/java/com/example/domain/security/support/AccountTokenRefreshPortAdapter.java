package com.example.domain.security.support;

import com.example.domain.account.payload.response.RefreshTokenResponse;
import com.example.domain.account.support.AccountTokenRefreshPort;
import com.example.domain.security.service.command.JwtTokenRefreshCommandService;
import com.example.global.security.payload.RefreshTokenIssueCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountTokenRefreshPortAdapter implements AccountTokenRefreshPort {

    private final JwtTokenRefreshCommandService jwtTokenRefreshCommandService;

    @Override
    public RefreshTokenResponse refreshTokens(String refreshToken) {
        return jwtTokenRefreshCommandService.refreshTokens(RefreshTokenIssueCommand.of(refreshToken));
    }
}
