package com.example.global.security.blacklist.payload.dto;

/**
 * 블랙리스트 토큰 해시 조회용 Query DTO
 */
public record BlacklistedTokenHashQuery(
        String tokenHash
) {

    public static BlacklistedTokenHashQuery of(final String tokenHash) {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash는 필수입니다.");
        }
        return new BlacklistedTokenHashQuery(tokenHash);
    }
}
