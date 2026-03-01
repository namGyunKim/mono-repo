package com.example.global.payload.response;

/**
 * 필드 단위 에러 상세 정보 DTO
 *
 * <p>
 * - DTO는 record 사용
 * - 외부에서 new 호출을 금지하기 위해 정적 팩토리 메서드를 제공합니다.
 * </p>
 */
public record ApiErrorDetail(
        String field,
        String reason
) {

    public ApiErrorDetail {
        if (field == null) {
            field = "";
        }
        if (reason == null) {
            reason = "";
        }
    }

    public static ApiErrorDetail of(final String field, final String reason) {
        return new ApiErrorDetail(field, reason);
    }
}
