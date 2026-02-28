package com.example.global.exception;

import com.example.global.exception.enums.ErrorCode;
import lombok.Getter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 전역 예외 베이스 클래스
 * - 에러 코드와 사용자용 메시지(errorDetailMessage)를 분리하여 보관합니다.
 * - 디버그용 스택 트레이스는 별도 필드로 보관합니다.
 */
@Getter
public abstract class BaseAppException extends RuntimeException {

    private static final String STACKTRACE_CAPTURE_PROPERTY = "app.exception.capture-stacktrace";
    private static final boolean CAPTURE_STACKTRACE = resolveCaptureStacktrace();

    private final ErrorCode errorCode;
    private final String errorDetailMessage;
    private final String debugStackTrace;

    protected BaseAppException(final ErrorCode errorCode) {
        super(resolveDetailMessage(errorCode, null));
        this.errorCode = errorCode;
        this.errorDetailMessage = resolveDetailMessage(errorCode, null);
        this.debugStackTrace = captureStackTrace(this);
    }

    protected BaseAppException(final ErrorCode errorCode, final String errorDetailMessage) {
        super(resolveDetailMessage(errorCode, errorDetailMessage));
        this.errorCode = errorCode;
        this.errorDetailMessage = resolveDetailMessage(errorCode, errorDetailMessage);
        this.debugStackTrace = captureStackTrace(this);
    }

    protected BaseAppException(final ErrorCode errorCode, final Exception exception) {
        super(exception);
        this.errorCode = errorCode;
        this.errorDetailMessage = resolveDetailMessage(errorCode, exception != null ? exception.getMessage() : null);
        this.debugStackTrace = captureStackTrace(exception);
    }

    private static String resolveDetailMessage(final ErrorCode errorCode, final String detailMessage) {
        if (detailMessage != null && !detailMessage.isBlank()) {
            return detailMessage;
        }
        return errorCode != null ? errorCode.getErrorMessage() : "";
    }

    private static String captureStackTrace(final Throwable exception) {
        if (!CAPTURE_STACKTRACE) {
            return null;
        }
        return stackTraceOf(exception);
    }

    private static String stackTraceOf(final Throwable exception) {
        if (exception == null) {
            return null;
        }
        final StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }

    private static boolean resolveCaptureStacktrace() {
        final String property = System.getProperty(STACKTRACE_CAPTURE_PROPERTY);
        if (property != null && !property.isBlank()) {
            return Boolean.parseBoolean(property);
        }
        final String env = System.getenv("APP_EXCEPTION_CAPTURE_STACKTRACE");
        if (env != null && !env.isBlank()) {
            return Boolean.parseBoolean(env);
        }
        return true;
    }
}
