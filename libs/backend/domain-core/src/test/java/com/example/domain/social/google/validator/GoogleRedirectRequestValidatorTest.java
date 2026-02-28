package com.example.domain.social.google.validator;

import com.example.domain.social.google.payload.request.GoogleRedirectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;

class GoogleRedirectRequestValidatorTest {

    private final GoogleRedirectRequestValidator validator = new GoogleRedirectRequestValidator();

    @Test
    void supports_GoogleRedirectRequest_returns_true() {
        assertThat(validator.supports(GoogleRedirectRequest.class)).isTrue();
    }

    @Test
    void supports_other_class_returns_false() {
        assertThat(validator.supports(String.class)).isFalse();
    }

    @Test
    void supports_null_returns_false() {
        assertThat(validator.supports(null)).isFalse();
    }

    @Test
    void validate_error_present_skips_code_state_validation() {
        final GoogleRedirectRequest request = GoogleRedirectRequest.of(null, null, "access_denied", "User denied");
        final Errors errors = new BeanPropertyBindingResult(request, "googleRedirectRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_missing_code_rejected() {
        final GoogleRedirectRequest request = GoogleRedirectRequest.of(null, "state-value", null, null);
        final Errors errors = new BeanPropertyBindingResult(request, "googleRedirectRequest");

        validator.validate(request, errors);

        assertThat(errors.hasFieldErrors("code")).isTrue();
    }

    @Test
    void validate_missing_state_rejected() {
        final GoogleRedirectRequest request = GoogleRedirectRequest.of("auth-code", null, null, null);
        final Errors errors = new BeanPropertyBindingResult(request, "googleRedirectRequest");

        validator.validate(request, errors);

        assertThat(errors.hasFieldErrors("state")).isTrue();
    }

    @Test
    void validate_valid_code_and_state_no_errors() {
        final GoogleRedirectRequest request = GoogleRedirectRequest.of("auth-code", "state-value", null, null);
        final Errors errors = new BeanPropertyBindingResult(request, "googleRedirectRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }
}
