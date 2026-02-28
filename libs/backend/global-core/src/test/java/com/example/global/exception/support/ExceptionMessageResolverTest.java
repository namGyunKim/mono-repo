package com.example.global.exception.support;

import com.example.global.exception.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionMessageResolverTest {

    private final ExceptionMessageResolver resolver = new ExceptionMessageResolver();

    @Test
    void resolveMessage_null_exception_returns_fallback() {
        assertThat(resolver.resolveMessage(null, "폴백")).isEqualTo("폴백");
    }

    @Test
    void resolveMessage_exception_with_null_message_returns_fallback() {
        final Exception e = new Exception((String) null);
        assertThat(resolver.resolveMessage(e, "폴백")).isEqualTo("폴백");
    }

    @Test
    void resolveMessage_exception_with_blank_message_returns_fallback() {
        final Exception e = new Exception("   ");
        assertThat(resolver.resolveMessage(e, "폴백")).isEqualTo("폴백");
    }

    @Test
    void resolveMessage_exception_with_message_returns_message() {
        final Exception e = new Exception("에러 발생");
        assertThat(resolver.resolveMessage(e, "폴백")).isEqualTo("에러 발생");
    }

    @Test
    void resolveDetailMessage_null_errorCode_delegates_to_resolveMessage() {
        final Exception e = new Exception("에러");
        assertThat(resolver.resolveDetailMessage(e, null)).isEqualTo("에러");
    }

    @Test
    void resolveDetailMessage_API_VERSION_INVALID_returns_errorCode_message() {
        final Exception e = new Exception("커스텀 메시지");
        assertThat(resolver.resolveDetailMessage(e, ErrorCode.API_VERSION_INVALID))
                .isEqualTo(ErrorCode.API_VERSION_INVALID.getErrorMessage());
    }

    @Test
    void resolveDetailMessage_other_errorCode_with_message_returns_exception_message() {
        final Exception e = new Exception("내 메시지");
        assertThat(resolver.resolveDetailMessage(e, ErrorCode.INPUT_VALUE_INVALID))
                .isEqualTo("내 메시지");
    }

    @Test
    void resolveDetailMessage_other_errorCode_without_message_returns_errorCode_message() {
        final Exception e = new Exception((String) null);
        assertThat(resolver.resolveDetailMessage(e, ErrorCode.INPUT_VALUE_INVALID))
                .isEqualTo(ErrorCode.INPUT_VALUE_INVALID.getErrorMessage());
    }
}
