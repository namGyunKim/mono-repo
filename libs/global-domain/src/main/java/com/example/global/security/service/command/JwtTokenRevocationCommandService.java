package com.example.global.security.service.command;

import com.example.domain.member.entity.Member;
import com.example.domain.member.repository.MemberRepository;
import com.example.global.security.blacklist.payload.dto.BlacklistedTokenRegisterCommand;
import com.example.global.security.blacklist.service.command.BlacklistedTokenCommandService;
import com.example.global.security.payload.SecurityLogoutCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class JwtTokenRevocationCommandService {

    private final BlacklistedTokenCommandService blacklistedTokenCommandService;
    private final MemberRepository memberRepository;

    @Transactional
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

        Member managedMember = memberRepository.findById(memberId).orElse(null);
        revokeManagedMemberTokens(managedMember, accessToken);
    }

    private void revokeManagedMemberTokens(Member managedMember, String accessToken) {
        if (managedMember == null) {
            return;
        }

        if (StringUtils.hasText(accessToken)) {
            blacklistedTokenCommandService.blacklistToken(BlacklistedTokenRegisterCommand.of(accessToken));
        }

        managedMember.rotateTokenVersion();
        managedMember.invalidateRefreshTokenEncrypted();
    }
}
