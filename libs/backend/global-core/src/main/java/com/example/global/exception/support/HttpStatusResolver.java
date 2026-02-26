package com.example.global.exception.support;

import com.example.global.exception.enums.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class HttpStatusResolver {

    public HttpStatus resolve(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.BAD_REQUEST;
        }

        return switch (errorCode) {
            case PAGE_NOT_EXIST, FILE_NOT_FOUND, MEMBER_NOT_EXIST -> HttpStatus.NOT_FOUND;
            case ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case AUTHENTICATION_REQUIRED,
                 AUTHENTICATION_FAILED,
                 REFRESH_TOKEN_INVALID,
                 REFRESH_TOKEN_EXPIRED,
                 REFRESH_TOKEN_REVOKED -> HttpStatus.UNAUTHORIZED;
            case METHOD_NOT_SUPPORTED -> HttpStatus.METHOD_NOT_ALLOWED;
            case INTERNAL_SERVER_ERROR, DATA_ACCESS_ERROR, FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
