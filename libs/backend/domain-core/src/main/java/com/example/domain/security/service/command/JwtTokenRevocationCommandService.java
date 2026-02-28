package com.example.domain.security.service.command;

import com.example.domain.security.blacklist.service.command.BlacklistedTokenCommandService;
import com.example.domain.security.support.SecurityMemberTokenPort;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.payload.SecurityLogoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenRevocationCommandService {

    private final BlacklistedTokenCommandService blacklistedTokenCommandService;
    private final SecurityMemberTokenPort securityMemberTokenPort;

    public void revokeOnLogout(SecurityLogoutCommand command) {
        if (command == null) {
            return;
        }

        revokeMemberTokensById(command.memberId(), command.accessToken());
    }

    private void revokeMemberTokensById(Long memberId, String accessToken) {
        if (memberId == null || memberId <= 0) {
            return;
        }

        if (StringUtils.hasText(accessToken)) {
            blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(accessToken));
        }

        securityMemberTokenPort.revokeTokens(memberId);
    }
}
