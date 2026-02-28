package com.example.global.security.payload;

/**
 * 로그인 토큰 발급 요청 DTO
 * <p>
 * - 컨트롤러/서비스 경계에서 엔티티 노출을 방지합니다.
 */
public record LoginTokenIssueCommand(
        Long memberId
) {

    public static LoginTokenIssueCommand of(final Long memberId) {
        return new LoginTokenIssueCommand(memberId);
    }
}
