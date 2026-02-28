package com.example.global.aop.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerLogMessageFactoryTest {

    private final ControllerLogMessageFactory factory = new ControllerLogMessageFactory();

    @Test
    void buildRequestLog_contains_REQ_prefix() {
        final String result = factory.buildRequestLog("abc12345", "127.0.0.1", "user1", "GET", "/api/members", "page=1");
        assertThat(result).contains("[REQ]");
    }

    @Test
    void buildRequestLog_contains_all_fields() {
        final String result = factory.buildRequestLog("abc12345", "127.0.0.1", "user1", "POST", "/api/members", "{}");
        assertThat(result).contains("abc12345", "127.0.0.1", "user1", "POST", "/api/members");
    }

    @Test
    void buildResponseLog_contains_RES_prefix() {
        final String result = factory.buildResponseLog("abc12345", 150, "200", "1024B");
        assertThat(result).contains("[RES]");
    }

    @Test
    void buildResponseLog_contains_elapsed_ms() {
        final String result = factory.buildResponseLog("abc12345", 150, "200", "1024B");
        assertThat(result).contains("150ms");
    }

    @Test
    void buildResponseLog_contains_status_and_size() {
        final String result = factory.buildResponseLog("abc12345", 50, "201", "512B");
        assertThat(result).contains("201", "512B");
    }
}
