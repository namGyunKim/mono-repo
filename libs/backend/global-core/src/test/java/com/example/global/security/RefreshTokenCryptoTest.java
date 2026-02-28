package com.example.global.security;

import com.example.global.security.jwt.JwtProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenCryptoTest {

    private static final String SECRET = "a-very-long-secret-key-for-testing-at-least-32-chars";

    private RefreshTokenCrypto createCrypto() {
        final JwtProperties props = JwtProperties.of("issuer", SECRET, Duration.ofHours(1), Duration.ofDays(7));
        return new RefreshTokenCrypto(props);
    }

    @Test
    void encrypt_null_returns_empty() {
        final RefreshTokenCrypto crypto = createCrypto();
        assertThat(crypto.encrypt(null)).isEmpty();
    }

    @Test
    void encrypt_blank_returns_empty() {
        final RefreshTokenCrypto crypto = createCrypto();
        assertThat(crypto.encrypt("   ")).isEmpty();
    }

    @Test
    void encrypt_valid_token_returns_non_empty_base64() {
        final RefreshTokenCrypto crypto = createCrypto();
        final String encrypted = crypto.encrypt("my-refresh-token");
        assertThat(encrypted).isNotBlank();
    }

    @Test
    void encrypt_same_token_produces_different_ciphertexts() {
        final RefreshTokenCrypto crypto = createCrypto();
        final String encrypted1 = crypto.encrypt("my-token");
        final String encrypted2 = crypto.encrypt("my-token");
        assertThat(encrypted1).isNotEqualTo(encrypted2);
    }

    @Test
    void decrypt_null_returns_empty() {
        final RefreshTokenCrypto crypto = createCrypto();
        assertThat(crypto.decrypt(null)).isEmpty();
    }

    @Test
    void decrypt_blank_returns_empty() {
        final RefreshTokenCrypto crypto = createCrypto();
        assertThat(crypto.decrypt("   ")).isEmpty();
    }

    @Test
    void encrypt_decrypt_round_trip_preserves_original() {
        final RefreshTokenCrypto crypto = createCrypto();
        final String original = "eyJhbGciOiJIUzI1NiJ9.test-payload.signature";
        final String encrypted = crypto.encrypt(original);
        final String decrypted = crypto.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(original);
    }

    @Test
    void decrypt_invalid_base64_throws_exception() {
        final RefreshTokenCrypto crypto = createCrypto();
        assertThatThrownBy(() -> crypto.decrypt("not-valid-base64!!!"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void construction_with_blank_secret_throws_exception() {
        final JwtProperties props = JwtProperties.of("issuer", "   ", Duration.ofHours(1), Duration.ofDays(7));
        assertThatThrownBy(() -> new RefreshTokenCrypto(props))
                .isInstanceOf(IllegalStateException.class);
    }
}
