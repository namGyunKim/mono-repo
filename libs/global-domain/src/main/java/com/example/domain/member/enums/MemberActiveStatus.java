package com.example.domain.member.enums;

import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.List;

@Getter
public enum MemberActiveStatus {
    ALL("전체"),
    ACTIVE("활성"),
    INACTIVE("비활성");

    private static final List<MemberActiveStatus> MEMBER_ALLOWED = List.of(ALL, ACTIVE, INACTIVE);

    private final String value;

    MemberActiveStatus(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MemberActiveStatus from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(MemberActiveStatus.class, requestValue);
    }

    public static boolean checkMember(MemberActiveStatus status) {
        return status != null && MEMBER_ALLOWED.contains(status);
    }
}
