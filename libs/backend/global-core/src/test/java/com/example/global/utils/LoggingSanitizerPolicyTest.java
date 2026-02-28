package com.example.global.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingSanitizerPolicyTest {

    @Test
    void isSensitiveField_null_returns_false() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField(null)).isFalse();
    }

    @Test
    void isSensitiveField_password_returns_true() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("password")).isTrue();
    }

    @Test
    void isSensitiveField_Password_case_insensitive_returns_true() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("Password")).isTrue();
    }

    @Test
    void isSensitiveField_accessToken_returns_true() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("accessToken")).isTrue();
    }

    @Test
    void isSensitiveField_authorization_returns_true() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("authorization")).isTrue();
    }

    @Test
    void isSensitiveField_clientSecret_returns_true() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("clientSecret")).isTrue();
    }

    @Test
    void isSensitiveField_secretKey_returns_true() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("secretKey")).isTrue();
    }

    @Test
    void isSensitiveField_myCustomToken_keyword_match_returns_true() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("myCustomToken")).isTrue();
    }

    @Test
    void isSensitiveField_username_returns_false() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("username")).isFalse();
    }

    @Test
    void isSensitiveField_description_returns_false() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("description")).isFalse();
    }

    @Test
    void isSensitiveField_blank_returns_false() {
        assertThat(LoggingSanitizerPolicy.isSensitiveField("   ")).isFalse();
    }

    @Test
    void getSensitiveFieldNames_returns_non_empty_set() {
        assertThat(LoggingSanitizerPolicy.getSensitiveFieldNames()).isNotEmpty();
        assertThat(LoggingSanitizerPolicy.getSensitiveFieldNames()).contains("password", "token", "authorization");
    }
}
