package com.example.global.aop.support;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerResponseInfoResolverTest {

    private final ControllerResponseInfoResolver resolver = new ControllerResponseInfoResolver();

    @Test
    void getResponseStatus_null_returns_unknown() {
        assertThat(resolver.getResponseStatus(null)).isEqualTo("알 수 없음");
    }

    @Test
    void getResponseStatus_valid_response_returns_status() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        assertThat(resolver.getResponseStatus(response)).isEqualTo("200");
    }

    @Test
    void getResponseSize_null_returns_unknown() {
        assertThat(resolver.getResponseSize(null)).isEqualTo("알 수 없음");
    }

    @Test
    void getResponseSize_content_length_header_returns_size() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader("Content-Length", "2048");
        assertThat(resolver.getResponseSize(response)).isEqualTo("2048B");
    }

    @Test
    void getResponseSize_no_header_regular_response_returns_unknown() {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        assertThat(resolver.getResponseSize(response)).isEqualTo("알 수 없음");
    }

    @Test
    void getResponseSize_ContentCachingResponseWrapper_returns_size() {
        final MockHttpServletResponse mockResponse = new MockHttpServletResponse();
        final ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(mockResponse);
        assertThat(resolver.getResponseSize(wrapper)).isEqualTo("0B");
    }
}
