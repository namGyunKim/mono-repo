package com.example.domain.social.google.support;

import com.example.domain.social.entity.SocialAccount;
import com.example.domain.social.google.payload.dto.GoogleSocialUnlinkCommand;
import com.example.global.utils.TraceIdUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 구글 소셜 연동 해제 전용 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleSocialUnlinkHandler {

    private final GoogleSocialAccountManager socialAccountManager;
    private final GoogleOauthTokenRevoker googleOauthTokenRevoker;

    public void unlink(GoogleSocialUnlinkCommand command) {
        if (isInvalidUnlinkCommand(command)) {
            return;
        }

        final Long memberId = command.memberId();
        final String loginId = command.loginId();

        final Optional<SocialAccount> socialAccount = findSocialAccountForUnlink(memberId, loginId);
        if (socialAccount.isEmpty()) {
            return;
        }

        final SocialAccount account = socialAccount.get();
        if (!hasRefreshTokenEncrypted(account, memberId, loginId)) {
            socialAccountManager.deleteIfExists(account);
            return;
        }

        revokeAndDeleteSocialAccount(account, memberId, loginId);
    }

    private boolean isInvalidUnlinkCommand(GoogleSocialUnlinkCommand command) {
        if (command == null) {
            log.warn("traceId={}, 구글 연동 해제 스킵: command=null", TraceIdUtils.resolveTraceId());
            return true;
        }

        final Long memberId = command.memberId();
        if (memberId == null || memberId <= 0) {
            log.warn("traceId={}, 구글 연동 해제 스킵: memberId 유효하지 않음", TraceIdUtils.resolveTraceId());
            return true;
        }

        return false;
    }

    private Optional<SocialAccount> findSocialAccountForUnlink(Long memberId, String loginId) {
        final Optional<SocialAccount> socialAccount = socialAccountManager.findByMemberId(memberId);
        if (socialAccount.isEmpty()) {
            log.info(
                    "traceId={}, 구글 연동 해제 스킵: 소셜 계정 없음 memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    memberId,
                    loginId
            );
        }
        return socialAccount;
    }

    private boolean hasRefreshTokenEncrypted(SocialAccount socialAccount, Long memberId, String loginId) {
        final String refreshTokenEncrypted = socialAccount.getRefreshTokenEncrypted();
        if (StringUtils.hasText(refreshTokenEncrypted)) {
            return true;
        }

        log.info(
                "traceId={}, 구글 토큰 revoke 스킵 후 소셜 계정 삭제 진행: refresh_token_encrypted 없음 memberId={}, loginId={}",
                TraceIdUtils.resolveTraceId(),
                memberId,
                loginId
        );
        return false;
    }

    private void revokeAndDeleteSocialAccount(SocialAccount socialAccount, Long memberId, String loginId) {
        try {
            googleOauthTokenRevoker.revoke(socialAccount, memberId, loginId);
            log.info(
                    "traceId={}, 구글 연동 해제 완료: memberId={}, loginId={}",
                    TraceIdUtils.resolveTraceId(),
                    memberId,
                    loginId
            );
        } finally {
            socialAccountManager.deleteIfExists(socialAccount);
        }
    }
}
