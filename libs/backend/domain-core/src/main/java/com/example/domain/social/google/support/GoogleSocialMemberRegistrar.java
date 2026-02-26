package com.example.domain.social.google.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.social.entity.SocialAccount;
import com.example.domain.social.google.payload.response.GoogleUserInfoResponse;
import com.example.global.exception.SocialException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 구글 소셜 로그인 회원 처리 전용 컴포넌트
 */
@Component
@RequiredArgsConstructor
public class GoogleSocialMemberRegistrar {
    private final GoogleSocialAccountManager socialAccountManager;
    private final GoogleSocialMemberCreator memberCreator;
    private final GoogleSocialActivityPublisher activityPublisher;

    public Member registerOrLogin(GoogleUserInfoResponse userInfo, String refreshToken) {
        String socialKey = requireSocialKey(userInfo);
        Optional<SocialAccount> socialAccount = socialAccountManager.findBySocialKey(socialKey);

        Optional<Member> activeMember = resolveActiveUserMember(socialAccount);
        if (activeMember.isPresent()) {
            Member existingMember = activeMember.get();
            socialAccount.ifPresent(account -> socialAccountManager.updateRefreshTokenIfPresent(account, refreshToken));
            activityPublisher.publishLogin(existingMember);
            return existingMember;
        }

        socialAccount.ifPresent(socialAccountManager::deleteIfExists);

        Member savedMember = memberCreator.register(userInfo, socialKey);
        SocialAccount newSocialAccount = socialAccountManager.create(savedMember, socialKey);
        socialAccountManager.updateRefreshTokenIfPresent(newSocialAccount, refreshToken);
        socialAccountManager.save(newSocialAccount);

        activityPublisher.publishJoin(savedMember);
        return savedMember;
    }

    private String requireSocialKey(GoogleUserInfoResponse userInfo) {
        if (userInfo == null || !StringUtils.hasText(userInfo.id())) {
            throw new SocialException(
                    ErrorCode.GOOGLE_API_GET_INFORMATION_ERROR,
                    "구글 사용자 식별자(id)가 비어있습니다."
            );
        }
        return userInfo.id().trim();
    }

    private Optional<Member> resolveActiveUserMember(Optional<SocialAccount> socialAccount) {
        return socialAccount
                .map(SocialAccount::getMember)
                .filter(member -> member.getActive() == MemberActiveStatus.ACTIVE)
                .filter(member -> member.getRole() == AccountRole.USER);
    }
}
