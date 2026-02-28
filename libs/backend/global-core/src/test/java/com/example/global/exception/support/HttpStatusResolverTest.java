package com.example.global.exception.support;

import com.example.global.exception.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class HttpStatusResolverTest {

    private final HttpStatusResolver resolver = new HttpStatusResolver();

    @Test
    void resolve_null_returns_BAD_REQUEST() {
        assertThat(resolver.resolve(null)).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void resolve_PAGE_NOT_EXIST_returns_NOT_FOUND() {
        assertThat(resolver.resolve(ErrorCode.PAGE_NOT_EXIST)).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void resolve_MEMBER_NOT_EXIST_returns_NOT_FOUND() {
        assertThat(resolver.resolve(ErrorCode.MEMBER_NOT_EXIST)).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void resolve_FILE_NOT_FOUND_returns_NOT_FOUND() {
        assertThat(resolver.resolve(ErrorCode.FILE_NOT_FOUND)).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void resolve_ACCESS_DENIED_returns_FORBIDDEN() {
        assertThat(resolver.resolve(ErrorCode.ACCESS_DENIED)).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void resolve_AUTHENTICATION_REQUIRED_returns_UNAUTHORIZED() {
        assertThat(resolver.resolve(ErrorCode.AUTHENTICATION_REQUIRED)).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void resolve_REFRESH_TOKEN_INVALID_returns_UNAUTHORIZED() {
        assertThat(resolver.resolve(ErrorCode.REFRESH_TOKEN_INVALID)).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void resolve_METHOD_NOT_SUPPORTED_returns_METHOD_NOT_ALLOWED() {
        assertThat(resolver.resolve(ErrorCode.METHOD_NOT_SUPPORTED)).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void resolve_INTERNAL_SERVER_ERROR_returns_500() {
        assertThat(resolver.resolve(ErrorCode.INTERNAL_SERVER_ERROR)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void resolve_FAILED_returns_500() {
        assertThat(resolver.resolve(ErrorCode.FAILED)).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void resolve_INPUT_VALUE_INVALID_returns_BAD_REQUEST() {
        assertThat(resolver.resolve(ErrorCode.INPUT_VALUE_INVALID)).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
