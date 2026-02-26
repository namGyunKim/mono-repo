package com.example.domain.log.validator;

import com.example.domain.log.payload.request.MemberLogRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 회원 로그 목록 조회 요청의 교차 필드(startAt/endAt) 검증
 */
@Component
public class MemberLogRequestPolicyValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz != null && MemberLogRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof MemberLogRequest request)) {
            errors.reject("memberLogRequest.invalid", "회원 로그 조회 요청 형식이 올바르지 않습니다.");
            return;
        }

        if (request.startAt() == null || request.endAt() == null) {
            return;
        }

        if (request.startAt().isAfter(request.endAt())) {
            errors.rejectValue("endAt", "endAt.invalidRange", "조회 종료 일시는 조회 시작 일시보다 같거나 이후여야 합니다.");
        }
    }
}
