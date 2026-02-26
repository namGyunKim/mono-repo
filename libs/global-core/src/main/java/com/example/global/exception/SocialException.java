package com.example.global.exception;

import com.example.global.exception.enums.ErrorCode;

// 소셜 로그인 예외 처리
public class SocialException extends BaseAppException {

    public SocialException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SocialException(ErrorCode errorCode, String errorDetailMessage) {
        super(errorCode, errorDetailMessage);
    }

    public SocialException(ErrorCode errorCode, Exception exception) {
        super(errorCode, exception);
    }
}
