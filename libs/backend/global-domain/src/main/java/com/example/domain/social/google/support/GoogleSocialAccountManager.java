package com.example.domain.social.google.support;

import com.example.domain.member.entity.Member;
import com.example.domain.social.entity.SocialAccount;
import com.example.domain.social.enums.SocialProvider;
import com.example.domain.social.payload.dto.SocialAccountKeyQuery;
import com.example.domain.social.payload.dto.SocialAccountMemberProviderQuery;
import com.example.domain.social.repository.SocialAccountRepository;
import com.example.global.security.RefreshTokenCrypto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GoogleSocialAccountManager {

    private final SocialAccountRepository socialAccountRepository;
    private final RefreshTokenCrypto refreshTokenCrypto;

    public Optional<SocialAccount> findBySocialKey(String socialKey) {
        return socialAccountRepository.findByProviderAndSocialKey(
                SocialAccountKeyQuery.of(SocialProvider.GOOGLE, socialKey)
        );
    }

    public Optional<SocialAccount> findByMemberId(Long memberId) {
        return socialAccountRepository.findByMemberIdAndProvider(
                SocialAccountMemberProviderQuery.of(memberId, SocialProvider.GOOGLE)
        );
    }

    public SocialAccount create(Member member, String socialKey) {
        return SocialAccount.from(member, SocialProvider.GOOGLE, socialKey);
    }

    public void save(SocialAccount socialAccount) {
        socialAccountRepository.save(socialAccount);
    }

    public void deleteIfExists(SocialAccount socialAccount) {
        if (socialAccount == null) {
            return;
        }
        socialAccountRepository.delete(socialAccount);
    }

    public void updateRefreshTokenIfPresent(SocialAccount socialAccount, String refreshToken) {
        if (socialAccount == null || !StringUtils.hasText(refreshToken)) {
            return;
        }
        String encrypted = refreshTokenCrypto.encrypt(refreshToken);
        socialAccount.updateRefreshTokenEncrypted(encrypted);
    }
}
