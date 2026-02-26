package com.example.global.enums;

import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.List;

@Getter
public enum GlobalOrderEnums {
    CREATE_ASC("생성일 오름차순"),
    CREATE_DESC("생성일 내림차순");

    private static final List<GlobalOrderEnums> DEFAULT_ALLOWED = List.of(CREATE_ASC, CREATE_DESC);

    private final String value;

    GlobalOrderEnums(String value) {
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
    public static GlobalOrderEnums from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(GlobalOrderEnums.class, requestValue);
    }

    public static boolean isAllowed(GlobalOrderEnums enums) {
        return enums != null && DEFAULT_ALLOWED.contains(enums);
    }

    public static boolean checkAdminMember(GlobalOrderEnums enums) {
        return isAllowed(enums);
    }

    public static boolean checkBoard(GlobalOrderEnums enums) {
        return isAllowed(enums);
    }
}
