package com.example.global.security.blacklist.payload.dto;

/**
 * 블랙리스트 여부 확인용 Query DTO
 */
public record BlacklistedTokenCheckQuery(
        String token
) {

    public static BlacklistedTokenCheckQuery of(String token) {
        return new BlacklistedTokenCheckQuery(token);
    }
}
