package com.example.domain.account.validator;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;

/**
 * Account 도메인 입력값 검증 유틸리티
 *
 * <p>
 * AccountCommandService, AccountQueryService에서 중복되는
 * CurrentAccountDTO 검증 로직을 통합합니다.
 * </p>
 */
public final class AccountInputValidator {

    private AccountInputValidator() {
    }

    /**
     * CurrentAccountDTO 기본 검증 (loginId, role 필수)
     * — QueryService 수준
     */
    public static void requireCurrentAccountBasic(CurrentAccountDTO currentAccount) {
        requireNonNull(currentAccount, "현재 사용자 정보가 비어있습니다.");
        requireHasText(currentAccount.loginId(), "현재 사용자 loginId는 필수입니다.");
        requireNonNull(currentAccount.role(), "현재 사용자 role은 필수입니다.");
    }

    /**
     * CurrentAccountDTO 전체 검증 (id, loginId, role, memberType, nickName 필수)
     * — CommandService 수준
     */
    public static void requireCurrentAccountFull(CurrentAccountDTO currentAccount) {
        requireCurrentAccountBasic(currentAccount);
        requireNonNull(currentAccount.id(), "현재 사용자 식별자는 필수입니다.");
        requireNonNull(currentAccount.memberType(), "현재 사용자 memberType은 필수입니다.");
        requireHasText(currentAccount.nickName(), "현재 사용자 nickName은 필수입니다.");
    }

    public static void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, message);
        }
    }

    public static void requireHasText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, message);
        }
    }
}
