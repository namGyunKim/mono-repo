package com.example.global.exception;

import com.example.global.exception.enums.ErrorCode;

// 소셜 로그인 예외 처리
public class SocialException extends BaseAppException {

    public SocialException(final ErrorCode errorCode) {
        super(errorCode);
    }

    public SocialException(final ErrorCode errorCode, final String errorDetailMessage) {
        super(errorCode, errorDetailMessage);
    }

    public SocialException(final ErrorCode errorCode, final Exception exception) {
        super(errorCode, exception);
    }
}
