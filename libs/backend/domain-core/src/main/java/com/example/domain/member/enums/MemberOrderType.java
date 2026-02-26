package com.example.domain.member.enums;

import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.List;

@Getter
public enum MemberOrderType {
    CREATE_ASC("생성일 오름차순"),
    CREATE_DESC("생성일 내림차순");

    private static final List<MemberOrderType> ADMIN_ALLOWED = List.of(CREATE_ASC, CREATE_DESC);

    private final String value;

    MemberOrderType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MemberOrderType from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(MemberOrderType.class, requestValue);
    }

    public static boolean checkAdminMember(MemberOrderType type) {
        return type != null && ADMIN_ALLOWED.contains(type);
    }
}
