package com.example.domain.account.validator;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.request.AccountAdminLoginRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 관리자 로그인 요청 전략
 */
@Component
public class AdminLoginRequestRoleStrategy implements LoginRequestRoleStrategy {

    private static final List<AccountRole> ADMIN_LOGIN_ROLES = List.of(AccountRole.ADMIN, AccountRole.SUPER_ADMIN);

    @Override
    public boolean supports(@Nullable Class<?> clazz) {
        return AccountAdminLoginRequest.class.equals(clazz);
    }

    @Override
    @Nullable
    public String resolveLoginId(@Nullable Object target) {
        if (target instanceof AccountAdminLoginRequest request) {
            return request.loginId();
        }
        return null;
    }

    @Override
    @Nullable
    public String resolvePassword(@Nullable Object target) {
        if (target instanceof AccountAdminLoginRequest request) {
            return request.password();
        }
        return null;
    }

    @Override
    public String resolveObjectName() {
        return "accountAdminLoginRequest";
    }

    @Override
    public List<AccountRole> allowedRoles() {
        return ADMIN_LOGIN_ROLES;
    }
}
