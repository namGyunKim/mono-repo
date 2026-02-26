package com.example.global.payload.response;

/**
 * 수정 응답 공통 DTO
 */
public record GlobalUpdateResponse(
        long id
) {

    public static GlobalUpdateResponse of(long id) {
        return new GlobalUpdateResponse(id);
    }
}
