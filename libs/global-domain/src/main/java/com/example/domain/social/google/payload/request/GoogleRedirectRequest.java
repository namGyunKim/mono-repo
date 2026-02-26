package com.example.domain.social.google.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 구글 OAuth 콜백(/api/social/v1/google/redirect)에서 사용하는 쿼리 파라미터 바인딩 전용 Request
 * <p>
 * - 컨트롤러에서 @RequestParam을 지양하기 위해 도입
 * - 구글은 성공 시 code/state를, 실패/거부 시 error/error_description 등을 전달할 수 있습니다.
 */
@Schema(description = "구글 OAuth 리다이렉트 콜백 바인딩 요청")
public record GoogleRedirectRequest(
        @Schema(description = "OAuth authorization code", example = "4/0Aea...")
        String code,
        @Schema(description = "OAuth state (CSRF 방지용)", example = "random-state")
        String state,
        @Schema(description = "OAuth error", example = "access_denied")
        String error,
        @Schema(description = "OAuth error description", example = "The user denied the request")
        String errorDescription
) {

    public static GoogleRedirectRequest of(String code, String state, String error, String errorDescription) {
        return new GoogleRedirectRequest(code, state, error, errorDescription);
    }
}
