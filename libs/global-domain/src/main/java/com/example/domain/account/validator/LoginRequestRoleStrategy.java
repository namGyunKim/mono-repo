package com.example.domain.account.validator;

import com.example.domain.account.enums.AccountRole;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * 로그인 요청 타입별 허용 권한 및 로그인 ID 추출 전략
 */
public interface LoginRequestRoleStrategy {

    boolean supports(@Nullable Class<?> clazz);

    @Nullable
    String resolveLoginId(@Nullable Object target);

    @Nullable
    default String resolvePassword(@Nullable Object target) {
        return null;
    }

    @Nullable
    default String resolveObjectName() {
        return null;
    }

    List<AccountRole> allowedRoles();
}
