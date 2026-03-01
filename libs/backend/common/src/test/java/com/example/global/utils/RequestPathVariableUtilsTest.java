package com.example.global.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RequestPathVariableUtilsTest {

    @Test
    void findPathVariable_null_request_returns_empty() {
        final Optional<String> result = RequestPathVariableUtils.findPathVariable(null, "id");
        assertThat(result).isEmpty();
    }

    @Test
    void findPathVariable_null_name_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Optional<String> result = RequestPathVariableUtils.findPathVariable(request, null);
        assertThat(result).isEmpty();
    }

    @Test
    void findPathVariable_blank_name_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Optional<String> result = RequestPathVariableUtils.findPathVariable(request, "   ");
        assertThat(result).isEmpty();
    }

    @Test
    void findPathVariable_no_attribute_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Optional<String> result = RequestPathVariableUtils.findPathVariable(request, "id");
        assertThat(result).isEmpty();
    }

    @Test
    void findPathVariable_attribute_not_map_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, "notAMap");
        final Optional<String> result = RequestPathVariableUtils.findPathVariable(request, "id");
        assertThat(result).isEmpty();
    }

    @Test
    void findPathVariable_key_found_returns_value() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "42"));
        final Optional<String> result = RequestPathVariableUtils.findPathVariable(request, "id");
        assertThat(result).hasValue("42");
    }

    @Test
    void findPathVariable_key_not_in_map_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("name", "john"));
        final Optional<String> result = RequestPathVariableUtils.findPathVariable(request, "id");
        assertThat(result).isEmpty();
    }

    @Test
    void findPathVariableAsLong_valid_numeric_returns_long() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "123"));
        final Optional<Long> result = RequestPathVariableUtils.findPathVariableAsLong(request, "id");
        assertThat(result).hasValue(123L);
    }

    @Test
    void findPathVariableAsLong_non_numeric_returns_empty() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("id", "abc"));
        final Optional<Long> result = RequestPathVariableUtils.findPathVariableAsLong(request, "id");
        assertThat(result).isEmpty();
    }
}
