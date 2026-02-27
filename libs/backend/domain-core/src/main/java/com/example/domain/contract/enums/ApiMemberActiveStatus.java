package com.example.domain.contract.enums;

import com.example.domain.member.enums.MemberActiveStatus;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum ApiMemberActiveStatus {
    ALL,
    ACTIVE,
    INACTIVE;

    private static final Map<String, ApiMemberActiveStatus> NAME_MAP = EnumParser.toNameMap(values());

    @JsonCreator
    public static ApiMemberActiveStatus from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

    public static ApiMemberActiveStatus fromDomain(MemberActiveStatus activeStatus) {
        if (activeStatus == null) {
            return null;
        }

        return switch (activeStatus) {
            case ALL -> ALL;
            case ACTIVE -> ACTIVE;
            case INACTIVE -> INACTIVE;
        };
    }

    public MemberActiveStatus toDomain() {
        return switch (this) {
            case ALL -> MemberActiveStatus.ALL;
            case ACTIVE -> MemberActiveStatus.ACTIVE;
            case INACTIVE -> MemberActiveStatus.INACTIVE;
        };
    }
}
