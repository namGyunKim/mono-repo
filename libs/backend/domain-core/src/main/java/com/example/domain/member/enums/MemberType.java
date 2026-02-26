package com.example.domain.member.enums;

import com.example.domain.account.enums.AccountRole;
import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum MemberType {

    GENERAL("일반"),
    GOOGLE("구글"),
    GUEST("손님"),
    ALL("전체");

    private static final Map<String, MemberType> NAME_MAP = EnumParser.toNameMap(values());
    private final String value;

    MemberType(String value) {
        this.value = value;
    }

    public static String toStrings() {
        return Arrays.stream(MemberType.values())
                .map(option -> "%s(%s)".formatted(option.name(), option.getValue()))
                .collect(Collectors.joining(","));
    }

    /**
     * 요청 값(String)으로 Enum을 매칭합니다.
     * <p>
     * [네이밍 표준]
     * - from(...): 외부 입력값을 Enum으로 변환하는 표준 메서드
     * </p>
     */
    @JsonCreator
    public static MemberType from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

    public boolean checkSocialType() {
        return this == GOOGLE;
    }

    // 소셜 타입의 기본 권한을 반환하는 메서드 추가
    public AccountRole getDefaultRole() {
        return AccountRole.USER;
    }
}
