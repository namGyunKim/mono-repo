package com.example.domain.account.validator;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountLoginCandidateView;
import com.example.domain.account.payload.dto.AccountLoginValidationQuery;
import com.example.domain.account.payload.request.AccountAdminLoginRequest;
import com.example.domain.account.payload.request.AccountUserLoginRequest;
import com.example.domain.account.service.query.AccountQueryService;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.enums.MemberType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Optional;

/**
 * 로그인 요청 DTO 유효성 검증
 *
 * <p>
 * - 일반 사용자 로그인 요청: {@link AccountUserLoginRequest}
 * - 관리자 로그인 요청: {@link AccountAdminLoginRequest}
 * <p>
 * - 계정 존재/상태/권한 등 **읽기 전용 정책 검증**만 수행합니다.
 * 비밀번호 검증은 Security의 AuthenticationProvider에 위임하고,
 * 여기서는 계정 존재 여부/상태 및 "요청 타입에 맞는 권한"만 확인합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class LoginAccountValidator implements Validator {

    private final AccountQueryService accountQueryService;
    private final List<LoginRequestRoleStrategy> roleStrategies;

    @Override
    public boolean supports(Class<?> clazz) {
        if (clazz == null) {
            return false;
        }
        return roleStrategies.stream().anyMatch(strategy -> strategy.supports(clazz));
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target == null) {
            errors.reject("loginRequest.invalid", "로그인 요청 형식이 올바르지 않습니다.");
            return;
        }

        final LoginRequestRoleStrategy strategy = resolveStrategy(target);
        if (strategy == null) {
            errors.reject("loginRequest.invalid", "로그인 요청 형식이 올바르지 않습니다.");
            return;
        }

        final String loginId = strategy.resolveLoginId(target);
        final List<AccountRole> allowedRoles = strategy.allowedRoles();

        // 기본값이 누락된 경우(Bean Validation 단계에서 처리)에는 DB 조회를 하지 않습니다.
        if (loginId == null || loginId.isBlank()) {
            return;
        }

        validateMemberRequest(loginId, allowedRoles, errors);
    }

    private LoginRequestRoleStrategy resolveStrategy(Object target) {
        final Class<?> targetClass = target.getClass();
        return roleStrategies.stream()
                .filter(strategy -> strategy.supports(targetClass))
                .findFirst()
                .orElse(null);
    }

    private void validateMemberRequest(String loginId, List<AccountRole> allowedRoles, Errors errors) {
        if (allowedRoles == null || allowedRoles.isEmpty()) {
            errors.rejectValue("loginId", "login.fail", "아이디가 존재하지 않거나 권한이 일치하지 않습니다.");
            return;
        }
        final Optional<AccountLoginCandidateView> candidateOptional = accountQueryService.findLoginCandidate(
                AccountLoginValidationQuery.of(loginId, allowedRoles)
        );

        if (candidateOptional.isEmpty()) {
            // UX 개선: 글로벌 에러가 아닌 필드 에러(loginId)로 내립니다.
            errors.rejectValue("loginId", "login.fail", "아이디가 존재하지 않거나 권한이 일치하지 않습니다.");
            return;
        }

        final AccountLoginCandidateView candidate = candidateOptional.get();

        // 활성화 상태 체크
        if (candidate.active() != MemberActiveStatus.ACTIVE) {
            errors.rejectValue("loginId", "login.fail", "비활성화된 계정입니다. 관리자에게 문의하세요.");
            return;
        }

        if (candidate.memberType() != MemberType.GENERAL) {
            errors.rejectValue("loginId", "login.fail", "비밀번호 로그인은 일반 계정만 지원합니다.");
        }

        // 비밀번호 검증은 Spring Security의 DaoAuthenticationProvider에서 수행하므로 생략합니다.
    }
}
