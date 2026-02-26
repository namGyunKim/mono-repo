package com.example.domain.social.google.payload.dto;

/**
 * 구글 소셜 연동 해제 요청 커맨드
 */
public record GoogleSocialUnlinkCommand(
        Long memberId,
        String loginId
) {

    public static GoogleSocialUnlinkCommand of(Long memberId, String loginId) {
        return new GoogleSocialUnlinkCommand(memberId, loginId);
    }
}
