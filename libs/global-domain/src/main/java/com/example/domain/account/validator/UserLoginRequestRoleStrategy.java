package com.example.domain.account.validator;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.request.AccountUserLoginRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 일반 사용자 로그인 요청 전략
 */
@Component
public class UserLoginRequestRoleStrategy implements LoginRequestRoleStrategy {

    private static final List<AccountRole> USER_LOGIN_ROLES = List.of(AccountRole.USER);

    @Override
    public boolean supports(@Nullable Class<?> clazz) {
        return AccountUserLoginRequest.class.equals(clazz);
    }

    @Override
    @Nullable
    public String resolveLoginId(@Nullable Object target) {
        if (target instanceof AccountUserLoginRequest request) {
            return request.loginId();
        }
        return null;
    }

    @Override
    @Nullable
    public String resolvePassword(@Nullable Object target) {
        if (target instanceof AccountUserLoginRequest request) {
            return request.password();
        }
        return null;
    }

    @Override
    public String resolveObjectName() {
        return "accountUserLoginRequest";
    }

    @Override
    public List<AccountRole> allowedRoles() {
        return USER_LOGIN_ROLES;
    }
}
