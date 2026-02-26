package com.example.global.exception;

import com.example.global.exception.enums.ErrorCode;

// 전역 예외 처리 클래스
public class GlobalException extends BaseAppException {

    public GlobalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public GlobalException(ErrorCode errorCode, String errorDetailMessage) {
        super(errorCode, errorDetailMessage);
    }

    public GlobalException(ErrorCode errorCode, Exception exception) {
        super(errorCode, exception);
    }
}
