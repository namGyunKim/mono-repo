package com.example.global.exception.support;

import com.example.global.exception.enums.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class ExceptionMessageResolver {

    public String resolveMessage(Exception e, String fallback) {
        if (e == null) {
            return fallback;
        }
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return fallback;
        }
        return msg;
    }

    public String resolveDetailMessage(Exception e, ErrorCode errorCode) {
        if (errorCode == null) {
            return resolveMessage(e, "");
        }
        if (errorCode == ErrorCode.API_VERSION_INVALID) {
            return errorCode.getErrorMessage();
        }
        return resolveMessage(e, errorCode.getErrorMessage());
    }
}
