package com.example.domain.log.validator;

import com.example.domain.log.payload.request.MemberLogRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MemberLogRequestPolicyValidatorTest {

    private final MemberLogRequestPolicyValidator validator = new MemberLogRequestPolicyValidator();

    @Test
    void supports_MemberLogRequest_returns_true() {
        assertThat(validator.supports(MemberLogRequest.class)).isTrue();
    }

    @Test
    void supports_other_class_returns_false() {
        assertThat(validator.supports(String.class)).isFalse();
    }

    @Test
    void validate_both_dates_null_no_errors() {
        final MemberLogRequest request = MemberLogRequest.of(1, 20, null, null, null, null, null, null);
        final Errors errors = new BeanPropertyBindingResult(request, "memberLogRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_startAt_after_endAt_rejected() {
        final LocalDateTime startAt = LocalDateTime.of(2026, 3, 1, 0, 0);
        final LocalDateTime endAt = LocalDateTime.of(2026, 2, 1, 0, 0);
        final MemberLogRequest request = MemberLogRequest.of(1, 20, null, null, null, null, startAt, endAt);
        final Errors errors = new BeanPropertyBindingResult(request, "memberLogRequest");

        validator.validate(request, errors);

        assertThat(errors.hasFieldErrors("endAt")).isTrue();
        assertThat(errors.getFieldError("endAt").getCode()).isEqualTo("endAt.invalidRange");
    }

    @Test
    void validate_startAt_before_endAt_no_errors() {
        final LocalDateTime startAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        final LocalDateTime endAt = LocalDateTime.of(2026, 2, 1, 0, 0);
        final MemberLogRequest request = MemberLogRequest.of(1, 20, null, null, null, null, startAt, endAt);
        final Errors errors = new BeanPropertyBindingResult(request, "memberLogRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_startAt_equals_endAt_no_errors() {
        final LocalDateTime dateTime = LocalDateTime.of(2026, 2, 1, 12, 0);
        final MemberLogRequest request = MemberLogRequest.of(1, 20, null, null, null, null, dateTime, dateTime);
        final Errors errors = new BeanPropertyBindingResult(request, "memberLogRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }
}
