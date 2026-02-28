package com.example.global.exception.support;

import com.example.global.exception.enums.ErrorCode;
import com.example.global.version.ApiVersioning;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ApiVersionErrorResolverTest {

    private final ApiVersionErrorResolver resolver = new ApiVersionErrorResolver();
    private static final ErrorCode FALLBACK = ErrorCode.REQUEST_BINDING_RESULT;

    @Test
    void resolve_null_request_returns_fallback() {
        assertThat(resolver.resolve(null, FALLBACK)).isEqualTo(FALLBACK);
    }

    @Test
    void resolve_non_api_path_returns_fallback() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/swagger-ui.html");
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(FALLBACK);
    }

    @Test
    void resolve_health_returns_fallback() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/health");
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(FALLBACK);
    }

    @Test
    void resolve_social_exact_returns_fallback() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/social");
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(FALLBACK);
    }

    @Test
    void resolve_social_subpath_returns_fallback() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/social/google/redirect");
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(FALLBACK);
    }

    @Test
    void resolve_api_members_no_header_returns_API_VERSION_REQUIRED() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members");
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(ErrorCode.API_VERSION_REQUIRED);
    }

    @Test
    void resolve_api_members_blank_header_returns_API_VERSION_REQUIRED() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members");
        request.addHeader(ApiVersioning.HEADER_NAME, "   ");
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(ErrorCode.API_VERSION_REQUIRED);
    }

    @Test
    void resolve_api_members_unsupported_version_returns_API_VERSION_INVALID() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members");
        request.addHeader(ApiVersioning.HEADER_NAME, "99.0");
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(ErrorCode.API_VERSION_INVALID);
    }

    @Test
    void resolve_api_members_valid_version_returns_fallback() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members");
        request.addHeader(ApiVersioning.HEADER_NAME, ApiVersioning.V1);
        assertThat(resolver.resolve(request, FALLBACK)).isEqualTo(FALLBACK);
    }
}
