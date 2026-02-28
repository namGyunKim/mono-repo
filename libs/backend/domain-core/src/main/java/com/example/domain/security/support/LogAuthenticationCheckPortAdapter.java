package com.example.domain.security.support;

import com.example.domain.log.support.LogAuthenticationCheckPort;
import com.example.domain.security.guard.MemberGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogAuthenticationCheckPortAdapter implements LogAuthenticationCheckPort {

    private final MemberGuard memberGuard;

    @Override
    public boolean isAuthenticated() {
        return memberGuard.isAuthenticated();
    }
}
