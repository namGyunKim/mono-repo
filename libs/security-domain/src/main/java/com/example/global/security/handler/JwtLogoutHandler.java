package com.example.global.security.handler;

import com.example.global.security.guard.MemberGuard;
import com.example.global.security.jwt.AccessTokenResolver;
import com.example.global.security.payload.SecurityLogoutCommand;
import com.example.global.security.service.command.JwtTokenRevocationCommandService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenRevocationCommandService jwtTokenRevocationCommandService;
    private final MemberGuard memberGuard;
    private final AccessTokenResolver accessTokenResolver;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String accessToken = accessTokenResolver.resolveAccessToken(request).orElse(null);
        memberGuard.getCurrentAccount()
                .map(account -> account.id())
                .ifPresent(memberId -> jwtTokenRevocationCommandService.revokeOnLogout(
                        SecurityLogoutCommand.of(memberId, accessToken)
                ));
    }
}
