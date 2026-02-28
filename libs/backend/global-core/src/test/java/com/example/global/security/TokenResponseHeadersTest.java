package com.example.global.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

class TokenResponseHeadersTest {

    @Test
    void of_sets_authorization_with_bearer_prefix() {
        final HttpHeaders headers = TokenResponseHeaders.of("myAccessToken", "myRefreshToken");
        assertThat(headers.getFirst(SecurityHeaders.AUTHORIZATION)).isEqualTo("Bearer myAccessToken");
    }

    @Test
    void of_sets_refresh_token_header() {
        final HttpHeaders headers = TokenResponseHeaders.of("myAccessToken", "myRefreshToken");
        assertThat(headers.getFirst(SecurityHeaders.REFRESH_TOKEN)).isEqualTo("myRefreshToken");
    }

    @Test
    void of_contains_both_header_keys() {
        final HttpHeaders headers = TokenResponseHeaders.of("access", "refresh");
        assertThat(headers.getFirst(SecurityHeaders.AUTHORIZATION)).isNotNull();
        assertThat(headers.getFirst(SecurityHeaders.REFRESH_TOKEN)).isNotNull();
    }
}
