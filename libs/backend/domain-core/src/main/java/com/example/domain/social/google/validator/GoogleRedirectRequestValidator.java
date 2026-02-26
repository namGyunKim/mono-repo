package com.example.domain.social.google.validator;

import com.example.domain.social.google.payload.request.GoogleRedirectRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Google OAuth redirect 요청(쿼리 파라미터) 검증
 *
 * <ul>
 *   <li>error가 존재하면(사용자 거부 등) 필수값 검증을 건너뛰고 후속 처리에서 예외로 종료합니다.</li>
 *   <li>error가 없으면 code/state는 필수이며, 누락 시 BindingResult 에러로 처리합니다.</li>
 *   <li>state/세션 검증 및 1회성 소비는 별도 Resolver에서 처리합니다.</li>
 * </ul>
 */
@Component
public class GoogleRedirectRequestValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz != null && GoogleRedirectRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof GoogleRedirectRequest redirectRequest)) {
            errors.reject("googleRedirectRequest.invalid", "구글 OAuth 요청 형식이 올바르지 않습니다.");
            return;
        }

        // OAuth 실패 케이스(access_denied 등)는 code가 없을 수 있으므로,
        // 필수값 검증을 건너뛰고 후속 처리에서 예외로 종료합니다.
        if (StringUtils.hasText(redirectRequest.error())) {
            return;
        }

        if (!StringUtils.hasText(redirectRequest.code())) {
            errors.rejectValue("code", "code.required", "구글 인증 코드(code)가 누락되었습니다.");
        }
        if (!StringUtils.hasText(redirectRequest.state())) {
            errors.rejectValue("state", "state.required", "구글 OAuth state가 누락되었습니다.");
        }

    }
}
