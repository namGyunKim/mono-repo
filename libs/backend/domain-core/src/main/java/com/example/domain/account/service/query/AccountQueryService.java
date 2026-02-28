package com.example.domain.account.service.query;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginCandidateView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.account.payload.dto.AccountLoginIdRoleQuery;
import com.example.domain.account.payload.dto.AccountLoginValidationQuery;
import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.account.payload.dto.LoginMemberView;
import com.example.domain.account.support.AccountMemberQueryPort;
import com.example.domain.account.validator.AccountInputValidator;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AccountQueryService {

    private final AccountMemberQueryPort accountMemberQueryPort;

    public LoginMemberView getLoginData(CurrentAccountDTO request) {
        validateCurrentAccount(request);

        LoginMemberView view = accountMemberQueryPort.findLoginMemberView(
                        AccountLoginIdRoleQuery.of(request.loginId(), request.role())
                )
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));
        if (view.active() != MemberActiveStatus.ACTIVE) {
            throw new GlobalException(ErrorCode.MEMBER_INACTIVE);
        }

        return view;
    }

    /**
     * 인증/인가 흐름 전용 조회 메서드
     *
     * <p>
     * - API 응답용 조회는 DTO 프로젝션(getLoginData 등)을 사용합니다.
     * - 이 메서드는 Security 내부 처리에 한해 사용합니다.
     * </p>
     */
    public AccountAuthMemberView findActiveMemberForAuthByLoginIdAndRole(AccountLoginIdRoleQuery request) {
        validateLoginIdRoleQuery(request);

        AccountAuthMemberView view = accountMemberQueryPort.findAuthMember(request)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));

        if (view.active() != MemberActiveStatus.ACTIVE) {
            throw new GlobalException(ErrorCode.MEMBER_INACTIVE);
        }
        return view;
    }

    /**
     * 인증/인가 흐름 전용 조회 메서드
     *
     * <p>
     * - API 응답용 조회는 DTO 프로젝션(getLoginData 등)을 사용합니다.
     * - 이 메서드는 Security 내부 처리에 한해 사용합니다.
     * </p>
     */
    public AccountAuthMemberView findActiveMemberForAuthByLoginId(AccountLoginIdQuery request) {
        validateLoginIdQuery(request);

        AccountAuthMemberView view = accountMemberQueryPort.findAuthMember(request)
                .orElseThrow(() -> new GlobalException(ErrorCode.MEMBER_NOT_EXIST));

        if (view.active() != MemberActiveStatus.ACTIVE) {
            throw new GlobalException(ErrorCode.MEMBER_INACTIVE);
        }
        return view;
    }

    public Optional<AccountLoginCandidateView> findLoginCandidate(AccountLoginValidationQuery request) {
        if (!isValidLoginValidationQuery(request)) {
            return Optional.empty();
        }
        return accountMemberQueryPort.findLoginCandidate(request);
    }

    private void validateCurrentAccount(CurrentAccountDTO request) {
        AccountInputValidator.requireCurrentAccountBasic(request);
    }

    private void validateLoginIdRoleQuery(AccountLoginIdRoleQuery request) {
        AccountInputValidator.requireNonNull(request, "로그인 조회 요청 값이 비어있습니다.");
        AccountInputValidator.requireHasText(request.loginId(), "loginId는 필수입니다.");
        AccountInputValidator.requireNonNull(request.role(), "role은 필수입니다.");
    }

    private void validateLoginIdQuery(AccountLoginIdQuery request) {
        AccountInputValidator.requireNonNull(request, "로그인 조회 요청 값이 비어있습니다.");
        AccountInputValidator.requireHasText(request.loginId(), "loginId는 필수입니다.");
    }

    private boolean isValidLoginValidationQuery(AccountLoginValidationQuery request) {
        if (request == null) {
            return false;
        }
        if (!StringUtils.hasText(request.loginId())) {
            return false;
        }
        return request.allowedRoles() != null && !request.allowedRoles().isEmpty();
    }
}
