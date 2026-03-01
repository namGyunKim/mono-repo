package com.example.global.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveLogMessageSanitizerTest {

    @Test
    void sanitize_null_returns_null() {
        assertThat(SensitiveLogMessageSanitizer.sanitize(null)).isNull();
    }

    @Test
    void sanitize_blank_returns_blank() {
        assertThat(SensitiveLogMessageSanitizer.sanitize("   ")).isEqualTo("   ");
    }

    @Test
    void sanitize_json_sensitive_field_masked() {
        final String input = "{\"password\":\"secret123\"}";
        final String result = SensitiveLogMessageSanitizer.sanitize(input);
        assertThat(result).contains("\"password\":\"***\"");
        assertThat(result).doesNotContain("secret123");
    }

    @Test
    void sanitize_json_non_sensitive_field_preserved() {
        final String input = "{\"username\":\"john\"}";
        final String result = SensitiveLogMessageSanitizer.sanitize(input);
        assertThat(result).contains("\"username\":\"john\"");
    }

    @Test
    void sanitize_key_value_sensitive_field_masked() {
        final String input = "password=secret123";
        final String result = SensitiveLogMessageSanitizer.sanitize(input);
        assertThat(result).contains("password=***");
        assertThat(result).doesNotContain("secret123");
    }

    @Test
    void sanitize_key_value_non_sensitive_preserved() {
        final String input = "username=john";
        final String result = SensitiveLogMessageSanitizer.sanitize(input);
        assertThat(result).contains("username=john");
    }

    @Test
    void sanitize_bearer_token_masked() {
        final String input = "Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature";
        final String result = SensitiveLogMessageSanitizer.sanitize(input);
        assertThat(result).contains("Bearer ***");
        assertThat(result).doesNotContain("eyJhbGciOiJIUzI1NiJ9");
    }

    @Test
    void sanitize_multiple_sensitive_fields() {
        final String input = "{\"password\":\"pass1\",\"accessToken\":\"tok1\"}";
        final String result = SensitiveLogMessageSanitizer.sanitize(input);
        assertThat(result).doesNotContain("pass1");
        assertThat(result).doesNotContain("tok1");
    }

    @Test
    void sanitize_no_sensitive_data_returns_unchanged() {
        final String input = "Normal log message with no secrets";
        final String result = SensitiveLogMessageSanitizer.sanitize(input);
        assertThat(result).isEqualTo(input);
    }
}
