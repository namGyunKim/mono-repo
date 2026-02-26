package com.example.global.event;

import com.example.global.exception.BaseAppException;
import com.example.global.exception.enums.ErrorCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * ExceptionEvent 생성 시 ErrorCode/메시지 해석 전용 매퍼
 */
public final class ExceptionEventMapper {

    private ExceptionEventMapper() {
    }

    public static ErrorMeta resolveErrorMeta(Exception exception) {
        if (exception instanceof BaseAppException appException) {
            return ErrorMeta.of(appException.getErrorCode(), appException.getErrorDetailMessage());
        }
        if (exception instanceof AccessDeniedException accessDeniedException) {
            String message = ExceptionEvent.resolveDetailMessage(accessDeniedException.getMessage(), ErrorCode.ACCESS_DENIED);
            return ErrorMeta.of(ErrorCode.ACCESS_DENIED, message);
        }
        if (exception instanceof NoHandlerFoundException noHandlerFoundException) {
            String message = ExceptionEvent.resolveDetailMessage(noHandlerFoundException.getMessage(), ErrorCode.PAGE_NOT_EXIST);
            return ErrorMeta.of(ErrorCode.PAGE_NOT_EXIST, message);
        }
        if (exception instanceof HttpRequestMethodNotSupportedException methodNotSupportedException) {
            String message = ExceptionEvent.resolveDetailMessage(methodNotSupportedException.getMessage(), ErrorCode.METHOD_NOT_SUPPORTED);
            return ErrorMeta.of(ErrorCode.METHOD_NOT_SUPPORTED, message);
        }
        String message = ExceptionEvent.resolveDetailMessage(exception != null ? exception.getMessage() : null, ErrorCode.FAILED);
        return ErrorMeta.of(ErrorCode.FAILED, message);
    }
}
