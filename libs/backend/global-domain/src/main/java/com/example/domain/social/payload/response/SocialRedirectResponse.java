package com.example.domain.social.payload.response;

/**
 * 소셜 로그인 리다이렉트 URL 응답 DTO
 */
public record SocialRedirectResponse(
        String redirectUrl
) {

    public static SocialRedirectResponse of(String redirectUrl) {
        return new SocialRedirectResponse(redirectUrl);
    }
}
