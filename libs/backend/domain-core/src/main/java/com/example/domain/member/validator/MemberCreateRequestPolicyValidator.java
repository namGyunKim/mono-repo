package com.example.domain.member.validator;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.request.MemberCreateRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 회원 생성 요청(MemberCreateRequest) 정책 검증
 *
 * <p>
 * - 컨트롤러에서 정책 검증/정규화 로직이 새어 나가지 않도록, @InitBinder 기반으로 수행합니다.
 * - 관리자 생성 요청의 role 필수 여부 및 지원하지 않는 role(GUEST) 요청을 차단합니다.
 * </p>
 */
@Component
public class MemberCreateRequestPolicyValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz != null && MemberCreateRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof MemberCreateRequest request)) {
            errors.reject("memberCreateRequest.invalid", "회원 생성 요청 형식이 올바르지 않습니다.");
            return;
        }

        AccountRole role = request.toDomainRole();
        if (role == null) {
            errors.rejectValue("role", "role.required", "권한은 필수입니다.");
            return;
        }

        if (role == AccountRole.GUEST) {
            errors.rejectValue("role", "role.unsupported", "GUEST 계정 생성은 지원하지 않습니다.");
        }
    }
}
