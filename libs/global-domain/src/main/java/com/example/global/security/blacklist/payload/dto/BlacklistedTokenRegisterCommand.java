package com.example.global.security.blacklist.payload.dto;

/**
 * 블랙리스트 토큰 등록용 Command DTO
 */
public record BlacklistedTokenRegisterCommand(
        String token
) {

    public static BlacklistedTokenRegisterCommand of(String token) {
        return new BlacklistedTokenRegisterCommand(token);
    }
}
