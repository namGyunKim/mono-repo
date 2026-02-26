package com.example.domain.member.validator;

import com.example.domain.member.payload.dto.MemberLoginIdDuplicateCheckQuery;
import com.example.domain.member.payload.dto.MemberNickNameDuplicateCheckQuery;
import com.example.domain.member.payload.request.MemberCreateRequest;
import com.example.domain.member.support.MemberUniquenessSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class MemberCreateValidator implements Validator {

    private final MemberUniquenessSupport memberUniquenessSupport;

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

        // [중요]
        // Validator는 Web(Request/PathVariable) 컨텍스트에 의존하지 않고,
        // 입력 값 자체의 유효성(예: 중복)만 검증하도록 유지합니다.
        validateCommon(request, errors);
    }

    private void validateCommon(MemberCreateRequest request, Errors errors) {
        // [최적화]
        // @NotBlank 등 기본 유효성에서 이미 실패한 경우에는 중복 검사(DB 조회)를 수행하지 않습니다.
        if (!errors.hasFieldErrors("loginId")
                && StringUtils.hasText(request.loginId())
                && memberUniquenessSupport.isLoginIdDuplicated(MemberLoginIdDuplicateCheckQuery.of(request.loginId()))) {
            errors.rejectValue("loginId", "loginId.duplicate", "이미 등록된 로그인 아이디입니다.");
        }

        if (!errors.hasFieldErrors("nickName")
                && StringUtils.hasText(request.nickName())
                && memberUniquenessSupport.isNickNameDuplicated(MemberNickNameDuplicateCheckQuery.of(request.nickName()))) {
            errors.rejectValue("nickName", "nickName.duplicate", "이미 등록된 닉네임입니다.");
        }
    }
}
