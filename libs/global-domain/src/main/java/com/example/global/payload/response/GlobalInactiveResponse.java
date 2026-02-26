package com.example.global.payload.response;

/**
 * 비활성/삭제 응답 공통 DTO
 */
public record GlobalInactiveResponse(
        long id
) {

    public static GlobalInactiveResponse of(long id) {
        return new GlobalInactiveResponse(id);
    }
}
