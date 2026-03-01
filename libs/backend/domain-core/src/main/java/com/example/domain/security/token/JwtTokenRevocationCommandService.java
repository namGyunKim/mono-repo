package com.example.domain.security.token;

import com.example.domain.security.token.BlacklistedTokenCommandService;
import com.example.domain.security.port.SecurityMemberTokenPort;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.payload.SecurityLogoutCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
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
            log.info("액세스 토큰 블랙리스트 등록 완료: memberId={}", memberId);
        }

        securityMemberTokenPort.revokeTokens(memberId);
        log.info("리프레시 토큰 폐기 완료: memberId={}", memberId);
    }
}
