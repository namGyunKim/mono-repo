package com.example.global.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class RequestUriUtilsTest {

    @Test
    void getPathWithinApplication_null_request_returns_empty() {
        assertThat(RequestUriUtils.getPathWithinApplication(null)).isEmpty();
    }

    @Test
    void getPathWithinApplication_null_uri_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(null);
        assertThat(RequestUriUtils.getPathWithinApplication(request)).isEmpty();
    }

    @Test
    void getPathWithinApplication_no_context_path_returns_full_uri() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/members");
        request.setContextPath("");
        assertThat(RequestUriUtils.getPathWithinApplication(request)).isEqualTo("/api/members");
    }

    @Test
    void getPathWithinApplication_strips_context_path() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/myapp/api/members");
        request.setContextPath("/myapp");
        assertThat(RequestUriUtils.getPathWithinApplication(request)).isEqualTo("/api/members");
    }

    @Test
    void getPathWithinApplication_uri_not_starting_with_context_returns_full_uri() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/other/api/members");
        request.setContextPath("/myapp");
        assertThat(RequestUriUtils.getPathWithinApplication(request)).isEqualTo("/other/api/members");
    }
}
