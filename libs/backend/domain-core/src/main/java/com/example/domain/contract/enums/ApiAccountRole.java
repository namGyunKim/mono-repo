package com.example.domain.contract.enums;

import com.example.domain.account.enums.AccountRole;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum ApiAccountRole {
    SUPER_ADMIN,
    ADMIN,
    USER,
    GUEST;

    private static final Map<String, ApiAccountRole> NAME_MAP = EnumParser.toNameMap(values());

    @JsonCreator
    public static ApiAccountRole from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

    public static ApiAccountRole fromDomain(AccountRole role) {
        if (role == null) {
            return null;
        }

        return switch (role) {
            case SUPER_ADMIN -> SUPER_ADMIN;
            case ADMIN -> ADMIN;
            case USER -> USER;
            case GUEST -> GUEST;
        };
    }

    public AccountRole toDomain() {
        return switch (this) {
            case SUPER_ADMIN -> AccountRole.SUPER_ADMIN;
            case ADMIN -> AccountRole.ADMIN;
            case USER -> AccountRole.USER;
            case GUEST -> AccountRole.GUEST;
        };
    }
}
