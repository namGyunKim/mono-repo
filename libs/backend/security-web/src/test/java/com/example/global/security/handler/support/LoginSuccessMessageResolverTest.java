package com.example.global.security.handler.support;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LoginSuccessMessageResolverTest {

    private final LoginSuccessMessageResolver resolver = new LoginSuccessMessageResolver();

    @Test
    void resolve_null_request_returns_default() {
        assertThat(resolver.resolve(null)).isEqualTo("로그인 성공");
    }

    @Test
    void resolve_admin_sessions_returns_admin_message() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/admin/sessions");
        assertThat(resolver.resolve(request)).isEqualTo("관리자 로그인 성공");
    }

    @Test
    void resolve_sessions_returns_general_message() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/sessions");
        assertThat(resolver.resolve(request)).isEqualTo("일반 로그인 성공");
    }

    @Test
    void resolve_with_context_path_strips_before_check() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/myapp");
        request.setRequestURI("/myapp/api/admin/sessions");
        assertThat(resolver.resolve(request)).isEqualTo("관리자 로그인 성공");
    }
}
