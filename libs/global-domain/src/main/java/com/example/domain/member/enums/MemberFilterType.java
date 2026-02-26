package com.example.domain.member.enums;

import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.List;

@Getter
public enum MemberFilterType {
    ALL("전체"),
    NICK_NAME("닉네임"),
    LOGIN_ID("로그인 아이디");

    private static final List<MemberFilterType> ADMIN_ALLOWED = List.of(ALL, NICK_NAME, LOGIN_ID);

    private final String value;

    MemberFilterType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static MemberFilterType from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(MemberFilterType.class, requestValue);
    }

    public static boolean checkAdminMember(MemberFilterType type) {
        return type != null && ADMIN_ALLOWED.contains(type);
    }
}
