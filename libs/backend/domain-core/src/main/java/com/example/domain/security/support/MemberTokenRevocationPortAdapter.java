package com.example.domain.security.support;

import com.example.domain.member.support.MemberTokenRevocationPort;
import com.example.domain.security.service.command.JwtTokenRevocationCommandService;
import com.example.global.security.payload.SecurityLogoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberTokenRevocationPortAdapter implements MemberTokenRevocationPort {

    private final JwtTokenRevocationCommandService jwtTokenRevocationCommandService;

    @Override
    public void revokeOnLogout(SecurityLogoutCommand command) {
        jwtTokenRevocationCommandService.revokeOnLogout(command);
    }
}
