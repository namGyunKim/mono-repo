package com.example.global.exception.advice;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.annotation.CurrentAccount;
import com.example.global.event.ExceptionEvent;
import com.example.global.exception.advice.support.ExceptionAdviceSupport;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogWriter;
import com.example.global.logging.RequestMeta;
import com.example.global.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@RestControllerAdvice
@RequiredArgsConstructor
public class StandardExceptionAdvice {

    private final ExceptionAdviceSupport support;
    private final ExceptionLogWriter exceptionLogWriter;

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
            AccessDeniedException e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            String message = support.resolveMessage(e, "권한이 없습니다.");
            RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logAccessDenied(meta, ErrorCode.ACCESS_DENIED, message);
            support.publishExceptionEvent(ExceptionEvent.from(e, ErrorCode.ACCESS_DENIED, message, resolvedAccount, request));
            return support.toResponse(ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
        });
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            ErrorCode errorCode = support.resolveApiVersionErrorCode(request, ErrorCode.PAGE_NOT_EXIST);
            String detailMessage = support.resolveDetailMessage(e, errorCode);
            exceptionLogWriter.logMessageOnly(meta, errorCode, detailMessage);
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    errorCode,
                    detailMessage,
                    resolvedAccount,
                    request
            ));
            return support.toResponse(errorCode, support.resolveHttpStatus(errorCode));
        });
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            ErrorCode errorCode = support.resolveApiVersionErrorCode(request, ErrorCode.PAGE_NOT_EXIST);
            String detailMessage = support.resolveDetailMessage(e, errorCode);
            exceptionLogWriter.logMessageOnly(meta, errorCode, detailMessage);
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    errorCode,
                    detailMessage,
                    resolvedAccount,
                    request
            ));
            return support.toResponse(errorCode, support.resolveHttpStatus(errorCode));
        });
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logMessageOnly(meta, ErrorCode.METHOD_NOT_SUPPORTED, support.resolveMessage(e, ""));
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    ErrorCode.METHOD_NOT_SUPPORTED,
                    e.getMessage(),
                    resolvedAccount,
                    request
            ));
            return support.toResponse(ErrorCode.METHOD_NOT_SUPPORTED, HttpStatus.METHOD_NOT_ALLOWED);
        });
    }
}
