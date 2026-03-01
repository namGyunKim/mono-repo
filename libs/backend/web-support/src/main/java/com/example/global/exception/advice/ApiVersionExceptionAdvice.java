package com.example.global.exception.advice;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.global.annotation.CurrentAccount;
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
import org.springframework.web.accept.InvalidApiVersionException;
import org.springframework.web.accept.MissingApiVersionException;
import org.springframework.web.accept.NotAcceptableApiVersionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RestControllerAdvice
@RequiredArgsConstructor
public class ApiVersionExceptionAdvice {

    private final ExceptionAdviceSupport support;
    private final ExceptionLogWriter exceptionLogWriter;

    @ExceptionHandler(MissingApiVersionException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingApiVersion(
            MissingApiVersionException e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logMessageOnly(meta, ErrorCode.API_VERSION_REQUIRED, support.resolveMessage(e, ""));
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    ErrorCode.API_VERSION_REQUIRED,
                    support.resolveMessage(e, ErrorCode.API_VERSION_REQUIRED.getErrorMessage()),
                    resolvedAccount,
                    request
            ));
            return support.toResponse(ErrorCode.API_VERSION_REQUIRED, HttpStatus.BAD_REQUEST);
        });
    }

    @ExceptionHandler(NotAcceptableApiVersionException.class)
    public ResponseEntity<ApiErrorResponse> handleNotAcceptableApiVersion(
            NotAcceptableApiVersionException e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            final ErrorCode errorCode = support.resolveApiVersionErrorCode(request, ErrorCode.API_VERSION_INVALID);
            exceptionLogWriter.logMessageOnly(meta, errorCode, support.resolveMessage(e, ""));
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    errorCode,
                    support.resolveMessage(e, errorCode.getErrorMessage()),
                    resolvedAccount,
                    request
            ));
            return support.toResponse(errorCode, HttpStatus.BAD_REQUEST);
        });
    }

    @ExceptionHandler(InvalidApiVersionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidApiVersion(
            InvalidApiVersionException e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            final ErrorCode errorCode = support.resolveApiVersionErrorCode(request, ErrorCode.API_VERSION_INVALID);
            exceptionLogWriter.logMessageOnly(meta, errorCode, support.resolveMessage(e, ""));
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    errorCode,
                    support.resolveMessage(e, errorCode.getErrorMessage()),
                    resolvedAccount,
                    request
            ));
            return support.toResponse(errorCode, HttpStatus.BAD_REQUEST);
        });
    }
}
