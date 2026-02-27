package com.example.domain.contract.enums;

import com.example.domain.member.enums.MemberType;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum ApiMemberType {
    GENERAL,
    GOOGLE,
    GUEST,
    ALL;

    private static final Map<String, ApiMemberType> NAME_MAP = EnumParser.toNameMap(values());

    @JsonCreator
    public static ApiMemberType from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

    public static ApiMemberType fromDomain(MemberType memberType) {
        if (memberType == null) {
            return null;
        }

        return switch (memberType) {
            case GENERAL -> GENERAL;
            case GOOGLE -> GOOGLE;
            case GUEST -> GUEST;
            case ALL -> ALL;
        };
    }

    public MemberType toDomain() {
        return switch (this) {
            case GENERAL -> MemberType.GENERAL;
            case GOOGLE -> MemberType.GOOGLE;
            case GUEST -> MemberType.GUEST;
            case ALL -> MemberType.ALL;
        };
    }
}
