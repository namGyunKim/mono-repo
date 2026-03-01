package com.example.global.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class TokenHashUtils {

    private static final String HASH_ALGORITHM = "SHA-256";

    private TokenHashUtils() {
    }

    public static String sha256(final String token) {
        if (token == null) {
            return "";
        }

        try {
            final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            final byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("지원하지 않는 해시 알고리즘입니다.", ex);
        }
    }
}
