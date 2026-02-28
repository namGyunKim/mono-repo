package com.example.global.exception.support;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.logging.RequestMeta;
import com.example.global.utils.LoggingSanitizerPolicy;
import com.example.global.utils.SensitiveLogMessageSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@Component
public class ExceptionLogWriter {

    public RequestMeta resolveRequestMeta(HttpServletRequest request) {
        return RequestMeta.from(request);
    }

    public void logTypeMismatch(RequestMeta meta, MethodArgumentTypeMismatchException e, ErrorCode errorCode) {
        String value = null;
        if (e != null && e.getName() != null) {
            final String raw = e.getValue() != null ? String.valueOf(e.getValue()) : "";
            value = LoggingSanitizerPolicy.isSensitiveField(e.getName()) ? "***" : raw;
        }
        log.warn(
                ExceptionLogTemplates.TYPE_MISMATCH_LOG_TEMPLATE.stripTrailing(),
                meta.traceId(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                e.getName(),
                value
        );
    }

    public void logMissingParameter(RequestMeta meta, MissingServletRequestParameterException e, ErrorCode errorCode) {
        log.warn(
                ExceptionLogTemplates.MISSING_PARAMETER_LOG_TEMPLATE.stripTrailing(),
                meta.traceId(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                e.getParameterName(),
                e.getParameterType()
        );
    }

    public void logMessageOnly(RequestMeta meta, ErrorCode errorCode, String message) {
        final String sanitizedMessage = sanitizeMessage(message);
        log.warn(
                ExceptionLogTemplates.MESSAGE_ONLY_LOG_TEMPLATE.stripTrailing(),
                meta.traceId(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                sanitizedMessage
        );
    }

    public void logAccessDenied(RequestMeta meta, ErrorCode errorCode, String message) {
        final String sanitizedMessage = sanitizeMessage(message);
        log.warn(
                ExceptionLogTemplates.ACCESS_DENIED_LOG_TEMPLATE.stripTrailing(),
                meta.traceId(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                sanitizedMessage
        );
    }

    public void logUnexpected(RequestMeta meta, CurrentAccountDTO account, Exception e, ErrorCode errorCode) {
        log.error(
                ExceptionLogTemplates.UNEXPECTED_EXCEPTION_LOG_TEMPLATE.stripTrailing(),
                meta.traceId(),
                meta.method(),
                meta.path(),
                resolveErrorName(errorCode),
                resolveErrorCode(errorCode),
                resolveErrorMessage(errorCode),
                account != null ? account.id() : null,
                account != null ? account.loginId() : null,
                e
        );
    }

    private String resolveErrorName(ErrorCode errorCode) {
        return errorCode != null ? errorCode.name() : "";
    }

    private String resolveErrorCode(ErrorCode errorCode) {
        return errorCode != null ? errorCode.getCode() : "";
    }

    private String resolveErrorMessage(ErrorCode errorCode) {
        return errorCode != null ? errorCode.getErrorMessage() : "";
    }

    private String sanitizeMessage(String message) {
        String sanitizedMessage = SensitiveLogMessageSanitizer.sanitize(message);
        return sanitizedMessage != null ? sanitizedMessage : "";
    }
}
