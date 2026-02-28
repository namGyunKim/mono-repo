package com.example.domain.social.support;

import com.example.domain.member.support.MemberSocialCleanupPort;
import com.example.domain.social.google.payload.dto.GoogleSocialUnlinkCommand;
import com.example.domain.social.google.support.GoogleSocialUnlinkHandler;
import com.example.domain.social.repository.SocialAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * MemberSocialCleanupPort 어댑터 — Social 도메인이 제공
 */
@Component
@RequiredArgsConstructor
public class MemberSocialCleanupPortAdapter implements MemberSocialCleanupPort {

    private final GoogleSocialUnlinkHandler googleSocialUnlinkHandler;
    private final SocialAccountRepository socialAccountRepository;

    @Override
    public void cleanupOnWithdraw(Long memberId, String loginId) {
        if (memberId == null || memberId <= 0) {
            return;
        }

        // 구글 소셜 연동 해제 (소셜 계정이 없으면 내부에서 무시)
        googleSocialUnlinkHandler.unlink(GoogleSocialUnlinkCommand.of(memberId, loginId));

        // 모든 소셜 계정 레코드 삭제
        socialAccountRepository.deleteByMemberId(memberId);
    }
}
