package com.example.global.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpExtractorTest {

    @Test
    void extract_null_request_returns_UNKNOWN() {
        assertThat(ClientIpExtractor.extract(null)).isEqualTo("UNKNOWN");
    }

    @Test
    void extract_cf_connecting_ip_is_prioritized() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-Connecting-IP", "1.2.3.4");
        request.addHeader("X-Real-IP", "5.6.7.8");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("1.2.3.4");
    }

    @Test
    void extract_x_real_ip_works() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "10.0.0.1");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("10.0.0.1");
    }

    @Test
    void extract_x_forwarded_for_returns_first_ip() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "1.1.1.1, 2.2.2.2, 3.3.3.3");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("1.1.1.1");
    }

    @Test
    void extract_forwarded_rfc7239_parsed() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Forwarded", "for=192.0.2.60;proto=http;by=203.0.113.43");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("192.0.2.60");
    }

    @Test
    void extract_forwarded_ipv6_bracket_format() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Forwarded", "for=\"[2001:db8::17]:4711\"");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("2001:db8::17");
    }

    @Test
    void extract_header_with_unknown_value_is_skipped() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-Connecting-IP", "unknown");
        request.setRemoteAddr("9.9.9.9");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("9.9.9.9");
    }

    @Test
    void extract_ipv4_port_format_strips_port() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "1.2.3.4:8080");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("1.2.3.4");
    }

    @Test
    void extract_falls_back_to_remoteAddr() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("127.0.0.1");
    }

    @Test
    void extract_remoteAddr_blank_returns_UNKNOWN() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("   ");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("UNKNOWN");
    }

    @Test
    void extract_empty_header_values_skipped() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-Connecting-IP", "  ");
        request.setRemoteAddr("10.10.10.10");
        assertThat(ClientIpExtractor.extract(request)).isEqualTo("10.10.10.10");
    }
}
