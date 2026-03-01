package com.example.global.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdUtilsTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void createTraceId_returns_8_char_string() {
        final String traceId = TraceIdUtils.createTraceId();
        assertThat(traceId).hasSize(8);
    }

    @Test
    void createTraceId_different_calls_produce_different_values() {
        final String id1 = TraceIdUtils.createTraceId();
        final String id2 = TraceIdUtils.createTraceId();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void resolveTraceId_mdc_has_value_returns_existing() {
        MDC.put(TraceIdUtils.TRACE_ID_KEY, "existing1");
        final String result = TraceIdUtils.resolveTraceId();
        assertThat(result).isEqualTo("existing1");
    }

    @Test
    void resolveTraceId_mdc_empty_creates_and_stores_new() {
        final String result = TraceIdUtils.resolveTraceId();
        assertThat(result).hasSize(8);
        assertThat(MDC.get(TraceIdUtils.TRACE_ID_KEY)).isEqualTo(result);
    }

    @Test
    void resolveTraceIdFromRequest_null_request_returns_new_traceId() {
        final String result = TraceIdUtils.resolveTraceIdFromRequest(null);
        assertThat(result).hasSize(8);
    }

    @Test
    void resolveTraceIdFromRequest_header_present_returns_header_value() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "header-id");
        final String result = TraceIdUtils.resolveTraceIdFromRequest(request);
        assertThat(result).isEqualTo("header-id");
    }

    @Test
    void resolveTraceIdFromRequest_blank_header_returns_new_traceId() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Trace-Id", "   ");
        final String result = TraceIdUtils.resolveTraceIdFromRequest(request);
        assertThat(result).hasSize(8);
    }

    @Test
    void resolveTraceHeaderName_returns_X_Trace_Id() {
        assertThat(TraceIdUtils.resolveTraceHeaderName()).isEqualTo("X-Trace-Id");
    }
}
