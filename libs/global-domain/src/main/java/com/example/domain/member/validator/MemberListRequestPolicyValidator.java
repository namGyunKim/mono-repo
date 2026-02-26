package com.example.domain.member.validator;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.payload.request.MemberListRequest;
import com.example.global.enums.GlobalActiveEnums;
import com.example.global.enums.GlobalFilterEnums;
import com.example.global.enums.GlobalOrderEnums;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * 회원 목록 조회 요청(MemberListRequest) 정책 검증
 *
 * <p>
 * - 관리자(ADMIN/SUPER_ADMIN) 조회에서만 허용되는 order/filter/active 조합을 검증합니다.
 * - 컨트롤러에서 if-else로 검증하지 않기 위해, @InitBinder 기반으로 수행합니다.
 * </p>
 */
@Component
public class MemberListRequestPolicyValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz != null && MemberListRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof MemberListRequest request)) {
            errors.reject("memberListRequest.invalid", "회원 목록 조회 요청 형식이 올바르지 않습니다.");
            return;
        }

        AccountRole role = request.role();
        if (role == null || role == AccountRole.USER) {
            return;
        }

        GlobalOrderEnums order = request.order();
        if (order != null && !GlobalOrderEnums.checkAdminMember(order)) {
            errors.rejectValue("order", "order.invalid", "관리자 정렬 기준이 아닙니다.");
        }

        GlobalFilterEnums filter = request.filter();
        if (filter != null && !GlobalFilterEnums.checkAdminMember(filter)) {
            errors.rejectValue("filter", "filter.invalid", "관리자 필터 기준이 아닙니다.");
        }

        GlobalActiveEnums active = request.active();
        if (active != null && !GlobalActiveEnums.checkMember(active)) {
            errors.rejectValue("active", "active.invalid", "회원 상태가 아닙니다.");
        }
    }

}
