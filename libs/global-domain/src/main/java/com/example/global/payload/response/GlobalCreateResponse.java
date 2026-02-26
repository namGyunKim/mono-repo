package com.example.global.payload.response;

/**
 * 생성 응답 공통 DTO
 */
public record GlobalCreateResponse(
        long id
) {

    public static GlobalCreateResponse of(long id) {
        return new GlobalCreateResponse(id);
    }
}
