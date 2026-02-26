package com.example.global.event;

import com.example.global.exception.enums.ErrorCode;

public record ErrorMeta(ErrorCode errorCode, String detailMessage) {

    public static ErrorMeta of(ErrorCode errorCode, String detailMessage) {
        return new ErrorMeta(errorCode, detailMessage);
    }
}
