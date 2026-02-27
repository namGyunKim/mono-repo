package com.example.domain.contract.enums;

import com.example.domain.member.enums.MemberOrderType;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum ApiMemberOrderType {
    CREATE_ASC,
    CREATE_DESC;

    private static final Map<String, ApiMemberOrderType> NAME_MAP = EnumParser.toNameMap(values());

    @JsonCreator
    public static ApiMemberOrderType from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

    public static ApiMemberOrderType fromDomain(MemberOrderType orderType) {
        if (orderType == null) {
            return null;
        }

        return switch (orderType) {
            case CREATE_ASC -> CREATE_ASC;
            case CREATE_DESC -> CREATE_DESC;
        };
    }

    public MemberOrderType toDomain() {
        return switch (this) {
            case CREATE_ASC -> MemberOrderType.CREATE_ASC;
            case CREATE_DESC -> MemberOrderType.CREATE_DESC;
        };
    }
}
