package com.example.global.utils;

import com.example.global.payload.response.ApiErrorDetail;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class LoginLoggingUtilsTest {

    private static final String PARAM_NAME = "loginId";
    private static final String ATTR_NAME = "loginIdAttribute";

    @Test
    void extractLoginId_null_request_returns_empty() {
        final Optional<String> result = LoginLoggingUtils.extractLoginId(null, PARAM_NAME, ATTR_NAME);
        assertThat(result).isEmpty();
    }

    @Test
    void extractLoginId_parameter_present_returns_value() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PARAM_NAME, "user@test.com");
        final Optional<String> result = LoginLoggingUtils.extractLoginId(request, PARAM_NAME, ATTR_NAME);
        assertThat(result).hasValue("user@test.com");
    }

    @Test
    void extractLoginId_parameter_absent_attribute_present_returns_attribute() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(ATTR_NAME, "admin@test.com");
        final Optional<String> result = LoginLoggingUtils.extractLoginId(request, PARAM_NAME, ATTR_NAME);
        assertThat(result).hasValue("admin@test.com");
    }

    @Test
    void extractLoginId_both_absent_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Optional<String> result = LoginLoggingUtils.extractLoginId(request, PARAM_NAME, ATTR_NAME);
        assertThat(result).isEmpty();
    }

    @Test
    void resolveLoginIdOrDefault_present_returns_value() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(PARAM_NAME, "user@test.com");
        final String result = LoginLoggingUtils.resolveLoginIdOrDefault(request, PARAM_NAME, ATTR_NAME, "DEFAULT");
        assertThat(result).isEqualTo("user@test.com");
    }

    @Test
    void resolveLoginIdOrDefault_absent_returns_default() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String result = LoginLoggingUtils.resolveLoginIdOrDefault(request, PARAM_NAME, ATTR_NAME, "DEFAULT");
        assertThat(result).isEqualTo("DEFAULT");
    }

    @Test
    void formatErrors_null_returns_empty_brackets() {
        assertThat(LoginLoggingUtils.formatErrors(null)).isEqualTo("[]");
    }

    @Test
    void formatErrors_empty_list_returns_empty_brackets() {
        assertThat(LoginLoggingUtils.formatErrors(Collections.emptyList())).isEqualTo("[]");
    }

    @Test
    void formatErrors_single_error_formatted() {
        final List<ApiErrorDetail> errors = List.of(ApiErrorDetail.of("email", "필수"));
        final String result = LoginLoggingUtils.formatErrors(errors);
        assertThat(result).contains("email=필수");
    }

    @Test
    void formatErrors_more_than_5_shows_truncation() {
        final List<ApiErrorDetail> errors = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> ApiErrorDetail.of("field" + i, "reason" + i))
                .toList();
        final String result = LoginLoggingUtils.formatErrors(errors);
        assertThat(result).contains("...");
    }

    @Test
    void safe_null_returns_empty() {
        assertThat(LoginLoggingUtils.safe(null)).isEmpty();
    }

    @Test
    void safe_non_null_passes_through() {
        assertThat(LoginLoggingUtils.safe("hello")).isEqualTo("hello");
    }
}
