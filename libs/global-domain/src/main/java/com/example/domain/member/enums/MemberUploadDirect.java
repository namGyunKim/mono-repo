package com.example.domain.member.enums;

import lombok.Getter;

@Getter
public enum MemberUploadDirect {
    MEMBER_PROFILE("memberProfile"),
    ;

    private final String value;

    MemberUploadDirect(String value) {
        this.value = value;
    }
}
