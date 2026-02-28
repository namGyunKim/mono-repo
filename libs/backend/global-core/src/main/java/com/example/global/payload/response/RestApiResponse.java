package com.example.global.payload.response;

/**
 * 공통 API 응답 Wrapper
 *
 * <p>
 * 성공 응답은 data 필드 기반으로 통일합니다.
 * DTO는 record + 정적 팩토리 메서드(of)를 제공합니다.
 * - 외부에서 new RestApiResponse<>(...) 호출을 금지하기 위한 표준 생성 메서드입니다.
 * </p>
 */
public record RestApiResponse<T>(T data) {

    /**
     * 표준 생성 메서드
     * - 성공 응답 전용 생성 메서드입니다.
     */
    public static <T> RestApiResponse<T> of(final T data) {
        return new RestApiResponse<>(data);
    }

    public static <T> RestApiResponse<T> success(final T data) {
        return of(data);
    }
}
