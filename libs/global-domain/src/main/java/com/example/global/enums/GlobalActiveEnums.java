package com.example.global.enums;

import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

import java.util.List;

@Getter
public enum GlobalActiveEnums {
    ALL("전체"),
    ACTIVE("활성"),
    INACTIVE("비활성");

    private static final List<GlobalActiveEnums> DEFAULT_ALLOWED = List.of(ALL, ACTIVE, INACTIVE);

    private final String value;

    GlobalActiveEnums(String value) {
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
    public static GlobalActiveEnums from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(GlobalActiveEnums.class, requestValue);
    }

    public static boolean isAllowed(GlobalActiveEnums enums) {
        return enums != null && DEFAULT_ALLOWED.contains(enums);
    }

    public static boolean checkMember(GlobalActiveEnums enums) {
        return isAllowed(enums);
    }

    public static boolean checkBoard(GlobalActiveEnums enums) {
        return isAllowed(enums);
    }

    public static boolean checkComment(GlobalActiveEnums enums) {
        return isAllowed(enums);
    }
}
