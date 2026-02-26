package com.example.domain.social.payload.dto;

import com.example.domain.social.enums.SocialProvider;

public record SocialAccountMemberProviderQuery(
        Long memberId,
        SocialProvider provider
) {

    public static SocialAccountMemberProviderQuery of(Long memberId, SocialProvider provider) {
        return new SocialAccountMemberProviderQuery(memberId, provider);
    }
}
