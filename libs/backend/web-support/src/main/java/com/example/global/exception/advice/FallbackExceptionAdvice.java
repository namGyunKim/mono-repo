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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
@RequiredArgsConstructor
public class FallbackExceptionAdvice {

    private final ExceptionAdviceSupport support;
    private final ExceptionLogWriter exceptionLogWriter;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(
            Exception e,
            @CurrentAccount CurrentAccountDTO account,
            HttpServletRequest request
    ) {
        return support.withFilterLogged(request, () -> {
            final CurrentAccountDTO resolvedAccount = support.resolveAccount(account);
            final RequestMeta meta = exceptionLogWriter.resolveRequestMeta(request);
            exceptionLogWriter.logUnexpected(meta, resolvedAccount, e, ErrorCode.INTERNAL_SERVER_ERROR);
            final String detailMessage = support.resolveMessage(e, ErrorCode.INTERNAL_SERVER_ERROR.getErrorMessage());
            support.publishExceptionEvent(ExceptionEvent.from(
                    e,
                    ErrorCode.INTERNAL_SERVER_ERROR,
                    detailMessage,
                    resolvedAccount,
                    request
            ));
            return support.toResponse(ErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
        });
    }
}
