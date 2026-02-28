package com.example.global.exception;

import com.example.global.exception.enums.ErrorCode;

// 전역 예외 처리 클래스
public class GlobalException extends BaseAppException {

    public GlobalException(final ErrorCode errorCode) {
        super(errorCode);
    }

    public GlobalException(final ErrorCode errorCode, final String errorDetailMessage) {
        super(errorCode, errorDetailMessage);
    }

    public GlobalException(final ErrorCode errorCode, final Exception exception) {
        super(errorCode, exception);
    }
}
