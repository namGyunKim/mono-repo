package com.example.domain.social.payload.response;

public record SocialLoginSuccessResponse(
        String status,
        String message
) {
    public SocialLoginSuccessResponse {
        if (status == null) {
            status = "";
        }
        if (message == null) {
            message = "";
        }
    }

    public static SocialLoginSuccessResponse of(String status, String message) {
        return new SocialLoginSuccessResponse(status, message);
    }

    public static SocialLoginSuccessResponse ok() {
        return of("OK", "소셜 로그인이 완료되었습니다.");
    }
}
