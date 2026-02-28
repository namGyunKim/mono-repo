package com.example.domain.log.event;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.global.event.ErrorMeta;
import com.example.global.exception.BaseAppException;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.SensitiveLogMessageSanitizer;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 예외 발생 시, 예외 정보를 담는 이벤트 객체
 *
 * <p>
 * [네이밍 규칙 표준화]
 * - from(...): 예외/요청 정보로부터 이벤트 객체로 "변환"하는 경우
 * </p>
 */
public record ExceptionEvent(
        String traceId,
        String requestPath,
        String requestMethod,
        String errorName,
        ErrorCode errorCode,
        String errorDetailMsg,
        String debugStackTrace,
        CurrentAccountDTO account,
        LocalDateTime createdAt,
        String clientIp
) {

    public ExceptionEvent {
        errorDetailMsg = sanitizeMessage(errorDetailMsg);
        debugStackTrace = sanitizeMessage(debugStackTrace);
    }

    public static ExceptionEvent from(
            Exception exception,
            ErrorCode errorCode,
            String errorDetailMsg,
            CurrentAccountDTO account,
            HttpServletRequest httpServletRequest
    ) {
        return from(exception, errorCode, errorDetailMsg, account, httpServletRequest, TraceIdUtils.resolveTraceId());
    }

    public static ExceptionEvent from(
            Exception exception,
            ErrorCode errorCode,
            String errorDetailMsg,
            CurrentAccountDTO account,
            HttpServletRequest httpServletRequest,
            String traceId
    ) {
        String requestPath = httpServletRequest != null ? httpServletRequest.getRequestURL().toString() : "";
        String requestMethod = httpServletRequest != null ? httpServletRequest.getMethod() : "";
        String clientIp = httpServletRequest != null ? ClientIpExtractor.extract(httpServletRequest) : "";

        String errorName = exception != null ? exception.getClass().getSimpleName() : "UnknownException";
        String debugStackTrace = resolveDebugStackTrace(exception);

        return new ExceptionEvent(
                traceId != null ? traceId : TraceIdUtils.resolveTraceId(),
                requestPath,
                requestMethod,
                errorName,
                errorCode,
                errorDetailMsg,
                debugStackTrace,
                account,
                LocalDateTime.now(),
                clientIp
        );
    }

    /**
     * 예외 타입에 맞는 ErrorCode/메시지를 자동 해석하는 표준 팩토리
     */
    public static ExceptionEvent from(
            Exception exception,
            CurrentAccountDTO account,
            HttpServletRequest httpServletRequest
    ) {
        ErrorMeta meta = ExceptionEventMapper.resolveErrorMeta(exception);
        return from(exception, meta.errorCode(), meta.detailMessage(), account, httpServletRequest);
    }

    static String resolveDetailMessage(String message, ErrorCode fallbackErrorCode) {
        if (message != null && !message.isBlank()) {
            return sanitizeMessage(message);
        }
        return sanitizeMessage(fallbackErrorCode != null ? fallbackErrorCode.getErrorMessage() : "");
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String resolveDebugStackTrace(Exception exception) {
        if (exception instanceof BaseAppException appException) {
            return appException.getDebugStackTrace();
        }
        return null;
    }

    private static String sanitizeMessage(String message) {
        return SensitiveLogMessageSanitizer.sanitize(message);
    }

    /**
     * 예외 이벤트를 사람이 읽기 좋은 문자열 형태로 변환합니다.
     */
    public String toLogString() {
        return """
                logStart=== === === === === === === === === === === === === === === === === === === === === === === === logStart
                Trace ID : %s
                Exception Title : %s
                Request Path : %s
                Request Method : %s
                Client IP : %s
                %s%screateDate : %s
                
                %s
                %s
                logEnd=== === === === === === === === === === === === === === === === === === === === === === === === logEnd
                """.formatted(
                nullToEmpty(traceId),
                nullToEmpty(errorName),
                nullToEmpty(requestPath),
                nullToEmpty(requestMethod),
                nullToEmpty(clientIp),
                buildAccountBlock(),
                buildErrorCodeBlock(),
                createdAt != null ? createdAt.toString() : "",
                nullToEmpty(errorDetailMsg),
                buildDebugBlock()
        ).stripTrailing();
    }

    private String buildAccountBlock() {
        if (account == null) {
            return "";
        }
        return """
                Account Member ID : %s
                Account role : %s
                Account ID : %s
                Account Nickname : %s
                """.formatted(
                account.id() != null ? account.id().toString() : "",
                nullToEmpty(account.role() != null ? account.role().name() : ""),
                nullToEmpty(account.loginId()),
                nullToEmpty(account.nickName())
        );
    }

    private String buildErrorCodeBlock() {
        if (errorCode == null) {
            return "";
        }
        return """
                Error Code & Msg : %s / %s
                """.formatted(errorCode.getCode(), errorCode.getErrorMessage());
    }

    private String buildDebugBlock() {
        if (debugStackTrace == null || debugStackTrace.isBlank()) {
            return "";
        }
        return """
                Debug StackTrace :
                %s
                """.formatted(debugStackTrace);
    }

    /**
     * 예외 이벤트를 구조화된(JSON) 로그용 Map으로 변환합니다.
     */
    public Map<String, Object> getStructuredLog() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", "exception");
        payload.put("traceId", traceId);
        payload.put("errorName", errorName);
        payload.put("errorCode", errorCode != null ? errorCode.getCode() : null);
        payload.put("errorMessage", errorCode != null ? errorCode.getErrorMessage() : null);
        payload.put("errorDetailMessage", errorDetailMsg);
        payload.put("debugStackTrace", debugStackTrace);
        payload.put("requestPath", requestPath);
        payload.put("requestMethod", requestMethod);
        payload.put("clientIp", clientIp);
        payload.put("createdAt", createdAt != null ? createdAt.toString() : null);
        payload.put("account", resolveAccountPayload());
        return payload;
    }

    private Map<String, Object> resolveAccountPayload() {
        if (account == null) {
            return null;
        }
        Map<String, Object> accountPayload = new LinkedHashMap<>();
        accountPayload.put("id", account.id());
        accountPayload.put("role", account.role() != null ? account.role().name() : null);
        accountPayload.put("loginId", account.loginId());
        accountPayload.put("nickName", account.nickName());
        return accountPayload;
    }

}
