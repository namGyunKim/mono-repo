package com.example.global.exception;

import com.example.global.exception.enums.ErrorCode;

// JWT 인터셉터 예외 처리
public class JWTInterceptorException extends BaseAppException {

    public JWTInterceptorException(ErrorCode errorCode) {
        super(errorCode);
    }

    public JWTInterceptorException(ErrorCode errorCode, String errorDetailMessage) {
        super(errorCode, errorDetailMessage);
    }

    public JWTInterceptorException(ErrorCode errorCode, Exception exception) {
        super(errorCode, exception);
    }
}
