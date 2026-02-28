package com.example.global.exception.support;

import com.example.global.payload.response.ApiErrorDetail;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationErrorMapperTest {

    private final ValidationErrorMapper mapper = new ValidationErrorMapper();

    @Test
    void resolveValidationErrors_non_binding_exception_returns_empty() {
        final List<ApiErrorDetail> result = mapper.resolveValidationErrors(new RuntimeException("test"));
        assertThat(result).isEmpty();
    }

    @Test
    void resolveValidationErrors_BindException_extracts_field_errors() throws Exception {
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.rejectValue(null, "error.code", "에러 메시지");
        final BindException ex = new BindException(bindingResult);

        final List<ApiErrorDetail> result = mapper.resolveValidationErrors(ex);
        assertThat(result).isNotEmpty();
    }

    @Test
    void resolveValidationErrors_MethodArgumentNotValidException_extracts_errors() throws Exception {
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.reject("global.error", "글로벌 에러");
        final MethodParameter param = new MethodParameter(
                Object.class.getMethod("toString"), -1
        );
        final MethodArgumentNotValidException ex = new MethodArgumentNotValidException(param, bindingResult);

        final List<ApiErrorDetail> result = mapper.resolveValidationErrors(ex);
        assertThat(result).hasSize(1);
    }

    @Test
    void resolveValidationDetailMessage_no_binding_result_returns_fallback() {
        final String result = mapper.resolveValidationDetailMessage(new RuntimeException(), "기본 메시지");
        assertThat(result).isEqualTo("기본 메시지");
    }

    @Test
    void resolveValidationDetailMessage_with_errors_returns_formatted_message() throws Exception {
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        bindingResult.reject("global.error", "글로벌 에러");
        final BindException ex = new BindException(bindingResult);

        final String result = mapper.resolveValidationDetailMessage(ex, "기본 메시지");
        assertThat(result).startsWith("요청 값 검증 실패:");
    }

    @Test
    void resolveValidationDetailMessage_empty_errors_returns_fallback() throws Exception {
        final BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        final BindException ex = new BindException(bindingResult);

        final String result = mapper.resolveValidationDetailMessage(ex, "기본 메시지");
        assertThat(result).isEqualTo("기본 메시지");
    }
}
