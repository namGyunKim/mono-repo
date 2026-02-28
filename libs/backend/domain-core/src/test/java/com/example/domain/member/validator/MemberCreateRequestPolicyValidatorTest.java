package com.example.domain.member.validator;

import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberType;
import com.example.domain.member.payload.request.MemberCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;

class MemberCreateRequestPolicyValidatorTest {

    private final MemberCreateRequestPolicyValidator validator = new MemberCreateRequestPolicyValidator();

    @Test
    void supports_MemberCreateRequest_returns_true() {
        assertThat(validator.supports(MemberCreateRequest.class)).isTrue();
    }

    @Test
    void supports_other_class_returns_false() {
        assertThat(validator.supports(String.class)).isFalse();
    }

    @Test
    void validate_null_role_rejected() {
        final MemberCreateRequest request = MemberCreateRequest.of("user1", "nick1", "pass", null, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");

        validator.validate(request, errors);

        assertThat(errors.hasFieldErrors("role")).isTrue();
        assertThat(errors.getFieldError("role").getCode()).isEqualTo("role.required");
    }

    @Test
    void validate_GUEST_role_rejected() {
        final MemberCreateRequest request = MemberCreateRequest.of("user1", "nick1", "pass", ApiAccountRole.GUEST, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");

        validator.validate(request, errors);

        assertThat(errors.hasFieldErrors("role")).isTrue();
        assertThat(errors.getFieldError("role").getCode()).isEqualTo("role.unsupported");
    }

    @Test
    void validate_USER_role_passes() {
        final MemberCreateRequest request = MemberCreateRequest.of("user1", "nick1", "pass", ApiAccountRole.USER, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_ADMIN_role_passes() {
        final MemberCreateRequest request = MemberCreateRequest.of("admin1", "nick1", "pass", ApiAccountRole.ADMIN, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }
}
