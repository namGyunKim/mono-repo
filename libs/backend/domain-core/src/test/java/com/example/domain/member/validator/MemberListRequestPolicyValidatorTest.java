package com.example.domain.member.validator;

import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberActiveStatus;
import com.example.domain.contract.enums.ApiMemberFilterType;
import com.example.domain.contract.enums.ApiMemberOrderType;
import com.example.domain.member.payload.request.MemberListRequest;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;

class MemberListRequestPolicyValidatorTest {

    private final MemberListRequestPolicyValidator validator = new MemberListRequestPolicyValidator();

    @Test
    void supports_MemberListRequest_returns_true() {
        assertThat(validator.supports(MemberListRequest.class)).isTrue();
    }

    @Test
    void supports_other_class_returns_false() {
        assertThat(validator.supports(String.class)).isFalse();
    }

    @Test
    void validate_USER_role_skips_admin_checks() {
        final MemberListRequest request = MemberListRequest.of(ApiAccountRole.USER, 1, 10, null, "", null, null);
        final Errors errors = new BeanPropertyBindingResult(request, "memberListRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_null_role_skips_admin_checks() {
        final MemberListRequest request = MemberListRequest.of(null, 1, 10, null, "", null, null);
        final Errors errors = new BeanPropertyBindingResult(request, "memberListRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_ADMIN_role_valid_params_passes() {
        final MemberListRequest request = MemberListRequest.of(
                ApiAccountRole.ADMIN, 1, 10,
                ApiMemberOrderType.CREATE_DESC, "", ApiMemberFilterType.ALL, ApiMemberActiveStatus.ALL
        );
        final Errors errors = new BeanPropertyBindingResult(request, "memberListRequest");

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }
}
