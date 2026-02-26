package com.example.domain.social.enums;

import com.example.global.utils.EnumParser;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;

public enum SocialProvider {

    GOOGLE,
    KAKAO,
    NAVER,
    APPLE;

    private static final Map<String, SocialProvider> NAME_MAP = EnumParser.toNameMap(values());

    @JsonCreator
    public static SocialProvider from(String requestValue) {
        return EnumParser.fromNameIgnoreCase(NAME_MAP, requestValue, null);
    }

}
