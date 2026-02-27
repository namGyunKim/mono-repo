package com.example.domain.contract.enums;

import com.example.domain.log.enums.LogType;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum ApiLogType {
    JOIN,
    LOGIN,
    LOGIN_FAIL,
    LOGOUT,
    EXCEPTION,
    UPDATE,
    INACTIVE,
    PASSWORD_CHANGE;

    private static final Map<String, ApiLogType> NAME_MAP = EnumParser.toNameMap(values());

    @JsonCreator
    public static ApiLogType from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

    public static ApiLogType fromDomain(LogType logType) {
        if (logType == null) {
            return null;
        }

        return switch (logType) {
            case JOIN -> JOIN;
            case LOGIN -> LOGIN;
            case LOGIN_FAIL -> LOGIN_FAIL;
            case LOGOUT -> LOGOUT;
            case EXCEPTION -> EXCEPTION;
            case UPDATE -> UPDATE;
            case INACTIVE -> INACTIVE;
            case PASSWORD_CHANGE -> PASSWORD_CHANGE;
        };
    }

    public LogType toDomain() {
        return switch (this) {
            case JOIN -> LogType.JOIN;
            case LOGIN -> LogType.LOGIN;
            case LOGIN_FAIL -> LogType.LOGIN_FAIL;
            case LOGOUT -> LogType.LOGOUT;
            case EXCEPTION -> LogType.EXCEPTION;
            case UPDATE -> LogType.UPDATE;
            case INACTIVE -> LogType.INACTIVE;
            case PASSWORD_CHANGE -> LogType.PASSWORD_CHANGE;
        };
    }
}
