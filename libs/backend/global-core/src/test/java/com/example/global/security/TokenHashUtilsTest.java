package com.example.global.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHashUtilsTest {

    @Test
    void sha256_null_returns_empty_string() {
        final String result = TokenHashUtils.sha256(null);
        assertThat(result).isEmpty();
    }

    @Test
    void sha256_non_null_returns_64_char_hex() {
        final String result = TokenHashUtils.sha256("test-token");
        assertThat(result).hasSize(64);
        assertThat(result).matches("[0-9a-f]+");
    }

    @Test
    void sha256_same_input_produces_same_hash() {
        final String hash1 = TokenHashUtils.sha256("my-token");
        final String hash2 = TokenHashUtils.sha256("my-token");
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void sha256_different_inputs_produce_different_hashes() {
        final String hash1 = TokenHashUtils.sha256("token-a");
        final String hash2 = TokenHashUtils.sha256("token-b");
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void sha256_empty_string_returns_valid_hash() {
        final String result = TokenHashUtils.sha256("");
        assertThat(result).hasSize(64);
    }
}
