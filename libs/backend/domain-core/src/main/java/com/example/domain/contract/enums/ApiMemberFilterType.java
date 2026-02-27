package com.example.domain.contract.enums;

import com.example.domain.member.enums.MemberFilterType;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum ApiMemberFilterType {
    ALL,
    NICK_NAME,
    LOGIN_ID;

    private static final Map<String, ApiMemberFilterType> NAME_MAP = EnumParser.toNameMap(values());

    @JsonCreator
    public static ApiMemberFilterType from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

    public static ApiMemberFilterType fromDomain(MemberFilterType filterType) {
        if (filterType == null) {
            return null;
        }

        return switch (filterType) {
            case ALL -> ALL;
            case NICK_NAME -> NICK_NAME;
            case LOGIN_ID -> LOGIN_ID;
        };
    }

    public MemberFilterType toDomain() {
        return switch (this) {
            case ALL -> MemberFilterType.ALL;
            case NICK_NAME -> MemberFilterType.NICK_NAME;
            case LOGIN_ID -> MemberFilterType.LOGIN_ID;
        };
    }
}
