package com.example.domain.account.validator;

import com.example.global.exception.GlobalException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountInputValidatorTest {

    @Test
    void requireNonNull_non_null_does_not_throw() {
        AccountInputValidator.requireNonNull("value", "에러");
    }

    @Test
    void requireNonNull_null_throws_GlobalException() {
        assertThatThrownBy(() -> AccountInputValidator.requireNonNull(null, "에러"))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void requireHasText_non_blank_does_not_throw() {
        AccountInputValidator.requireHasText("value", "에러");
    }

    @Test
    void requireHasText_null_throws_GlobalException() {
        assertThatThrownBy(() -> AccountInputValidator.requireHasText(null, "에러"))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void requireHasText_blank_throws_GlobalException() {
        assertThatThrownBy(() -> AccountInputValidator.requireHasText("   ", "에러"))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    void requireNonNull_exception_has_message() {
        assertThatThrownBy(() -> AccountInputValidator.requireNonNull(null, "커스텀 메시지"))
                .isInstanceOf(GlobalException.class)
                .hasMessageContaining("커스텀 메시지");
    }
}
