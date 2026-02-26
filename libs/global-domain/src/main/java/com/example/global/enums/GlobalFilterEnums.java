package com.example.global.enums;

import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum GlobalFilterEnums {
    ALL("전체"),
    NICK_NAME("닉네임"),
    LOGIN_ID("로그인 아이디"),
    TITLE("제목"),
    CONTENT("내용");

    private final String value;

    GlobalFilterEnums(String value) {
        this.value = value;
    }

    /**
     * 요청 값(String)으로 Enum을 매칭합니다.
     * <p>
     * [네이밍 표준]
     * - from(...): 외부 입력값을 Enum으로 변환하는 표준 메서드
     * </p>
     */
    @JsonCreator
    public static GlobalFilterEnums from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(GlobalFilterEnums.class, requestValue);
    }

    public static boolean checkAdminMember(GlobalFilterEnums enums) {
        List<GlobalFilterEnums> allowedValues = Arrays.asList(ALL, NICK_NAME, LOGIN_ID);
        return allowedValues.contains(enums);
    }

    public static boolean checkBoard(GlobalFilterEnums enums) {
        List<GlobalFilterEnums> allowedValues = Arrays.asList(ALL, NICK_NAME, TITLE, CONTENT);
        return allowedValues.contains(enums);
    }
}
