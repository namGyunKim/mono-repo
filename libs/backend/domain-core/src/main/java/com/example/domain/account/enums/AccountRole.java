package com.example.domain.account.enums;

import com.example.global.exception.GlobalException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum AccountRole {
    SUPER_ADMIN("최고 관리자"),
    ADMIN("관리자"),
    USER("사용자"),
    GUEST("손님");

    private final String value;

    AccountRole(String value) {
        this.value = value;
    }

    /**
     * 요청 값(String)으로 Enum을 매칭합니다.
     * <p>
     * [네이밍 표준]
     * - from(...): 외부 입력값을 Enum으로 변환하는 표준 메서드
     * </p>
     */
    @JsonCreator
    public static AccountRole from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(AccountRole.class, requestValue);
    }

    public static AccountRole fromRequired(String requestValue) {
        final AccountRole role = from(requestValue);
        if (role == null) {
            throw new GlobalException(ErrorCode.INVALID_PARAMETER, "유효하지 않은 role 값입니다: %s".formatted(requestValue));
        }
        return role;
    }
}
