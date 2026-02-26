package com.example.global.payload.response;

/**
 * 식별자(id)만 반환하는 공통 응답 DTO
 */
public record IdResponse(
        long id
) {

    public static IdResponse of(long id) {
        return new IdResponse(id);
    }
}
