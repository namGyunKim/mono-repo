package com.example.global.exception.advice.support;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.event.ExceptionEvent;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.*;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.guard.MemberGuard;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class ExceptionAdviceSupport {

    private final ExceptionEventPublisher exceptionEventPublisher;
    private final FilterLoggingMarker filterLoggingMarker;
    private final ApiErrorResponseFactory apiErrorResponseFactory;
    private final HttpStatusResolver httpStatusResolver;
    private final ApiVersionErrorResolver apiVersionErrorResolver;
    private final ValidationErrorMapper validationErrorMapper;
    private final ExceptionMessageResolver exceptionMessageResolver;
    private final MemberGuard memberGuard;

    public <T> T withFilterLogged(HttpServletRequest request, Supplier<T> action) {
        markFilterLogged(request);
        return action.get();
    }

    public void markFilterLogged(HttpServletRequest request) {
        filterLoggingMarker.markFilterLogged(request);
    }

    public void publishExceptionEvent(ExceptionEvent event) {
        exceptionEventPublisher.publish(event);
    }

    public CurrentAccountDTO resolveAccount(CurrentAccountDTO account) {
        if (account != null) {
            return account;
        }
        return memberGuard.getCurrentAccountOrGuest();
    }

    public ResponseEntity<ApiErrorResponse> toResponse(ErrorCode errorCode, HttpStatus status) {
        return apiErrorResponseFactory.toResponse(errorCode, status);
    }

    public ResponseEntity<ApiErrorResponse> toResponse(ErrorCode errorCode, HttpStatus status, List<ApiErrorDetail> errors) {
        return apiErrorResponseFactory.toResponse(errorCode, status, errors);
    }

    public HttpStatus resolveHttpStatus(ErrorCode errorCode) {
        return httpStatusResolver.resolve(errorCode);
    }

    public String resolveMessage(Exception e, String fallback) {
        return exceptionMessageResolver.resolveMessage(e, fallback);
    }

    public ErrorCode resolveApiVersionErrorCode(HttpServletRequest request, ErrorCode fallback) {
        return apiVersionErrorResolver.resolve(request, fallback);
    }

    public String resolveDetailMessage(Exception e, ErrorCode errorCode) {
        return exceptionMessageResolver.resolveDetailMessage(e, errorCode);
    }

    public List<ApiErrorDetail> resolveValidationErrors(Exception e) {
        return validationErrorMapper.resolveValidationErrors(e);
    }

    public String resolveValidationDetailMessage(Exception e, String fallback) {
        return validationErrorMapper.resolveValidationDetailMessage(e, fallback);
    }
}
