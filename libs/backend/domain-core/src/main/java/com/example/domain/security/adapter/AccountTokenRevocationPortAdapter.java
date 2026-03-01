package com.example.domain.security.adapter;

import com.example.domain.account.support.AccountTokenRevocationPort;
import com.example.domain.security.token.JwtTokenRevocationCommandService;
import com.example.global.security.payload.SecurityLogoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountTokenRevocationPortAdapter implements AccountTokenRevocationPort {

    private final JwtTokenRevocationCommandService jwtTokenRevocationCommandService;

    @Override
    public void revokeOnLogout(Long memberId, String accessToken) {
        jwtTokenRevocationCommandService.revokeOnLogout(SecurityLogoutCommand.of(memberId, accessToken));
    }
}
