package com.example.domain.member.validator;

import com.example.domain.contract.enums.ApiAccountRole;
import com.example.domain.contract.enums.ApiMemberType;
import com.example.domain.member.payload.request.MemberCreateRequest;
import com.example.domain.member.support.MemberUniquenessSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberCreateValidatorTest {

    @Mock
    private MemberUniquenessSupport memberUniquenessSupport;

    @InjectMocks
    private MemberCreateValidator validator;

    @Test
    void supports_MemberCreateRequest_returns_true() {
        assertThat(validator.supports(MemberCreateRequest.class)).isTrue();
    }

    @Test
    void supports_other_class_returns_false() {
        assertThat(validator.supports(String.class)).isFalse();
    }

    @Test
    void validate_duplicate_loginId_rejected() {
        final MemberCreateRequest request = MemberCreateRequest.of("user1", "nick1", "pass", ApiAccountRole.USER, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");
        given(memberUniquenessSupport.isLoginIdDuplicated(any())).willReturn(true);

        validator.validate(request, errors);

        assertThat(errors.hasFieldErrors("loginId")).isTrue();
        assertThat(errors.getFieldError("loginId").getCode()).isEqualTo("loginId.duplicate");
    }

    @Test
    void validate_duplicate_nickName_rejected() {
        final MemberCreateRequest request = MemberCreateRequest.of("user1", "nick1", "pass", ApiAccountRole.USER, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");
        given(memberUniquenessSupport.isLoginIdDuplicated(any())).willReturn(false);
        given(memberUniquenessSupport.isNickNameDuplicated(any())).willReturn(true);

        validator.validate(request, errors);

        assertThat(errors.hasFieldErrors("nickName")).isTrue();
        assertThat(errors.getFieldError("nickName").getCode()).isEqualTo("nickName.duplicate");
    }

    @Test
    void validate_unique_loginId_and_nickName_no_errors() {
        final MemberCreateRequest request = MemberCreateRequest.of("user1", "nick1", "pass", ApiAccountRole.USER, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");
        given(memberUniquenessSupport.isLoginIdDuplicated(any())).willReturn(false);
        given(memberUniquenessSupport.isNickNameDuplicated(any())).willReturn(false);

        validator.validate(request, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_existing_loginId_field_error_skips_duplicate_check() {
        final MemberCreateRequest request = MemberCreateRequest.of("user1", "nick1", "pass", ApiAccountRole.USER, ApiMemberType.GENERAL);
        final Errors errors = new BeanPropertyBindingResult(request, "memberCreateRequest");
        errors.rejectValue("loginId", "loginId.blank", "로그인 아이디 필수");
        given(memberUniquenessSupport.isNickNameDuplicated(any())).willReturn(false);

        validator.validate(request, errors);

        assertThat(errors.getFieldErrorCount("loginId")).isEqualTo(1);
    }
}
