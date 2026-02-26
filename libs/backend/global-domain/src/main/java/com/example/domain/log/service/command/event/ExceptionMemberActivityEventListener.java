package com.example.domain.log.service.command.event;

import com.example.domain.log.enums.LogType;
import com.example.domain.log.event.MemberActivityEvent;
import com.example.domain.log.payload.dto.MemberActivityPayload;
import com.example.domain.log.support.LogDetailsFormatter;
import com.example.global.event.ExceptionEvent;
import com.example.global.security.guard.MemberGuard;
import com.example.global.utils.SensitiveLogMessageSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExceptionMemberActivityEventListener {

    private static final long MIN_RECORDABLE_MEMBER_ID = 1L;

    private final MemberGuard memberGuard;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void handleExceptionEvent(ExceptionEvent event) {
        if (!shouldRecordExceptionActivity(event)) {
            return;
        }

        final var account = event.account();
        final String loginId = account.loginId();
        final Long memberId = account.id();
        final String target = "loginId=%s, memberId=%s".formatted(loginId, memberId);
        final String message = buildMessage(event);
        final MemberActivityPayload payload = MemberActivityPayload.of(
                loginId,
                memberId,
                LogType.EXCEPTION,
                LogDetailsFormatter.format(LogType.EXCEPTION, target, message),
                event.clientIp()
        );

        eventPublisher.publishEvent(MemberActivityEvent.from(payload));
    }

    private String buildMessage(ExceptionEvent event) {
        final String errorName = safeText(event.errorName(), "UnknownException");
        final String errorCodeName = event.errorCode() != null ? safeText(event.errorCode().name(), "UNKNOWN") : "UNKNOWN";
        final String errorCode = event.errorCode() != null ? safeText(event.errorCode().getCode(), "UNKNOWN") : "UNKNOWN";
        final String errorMessage = resolveErrorMessage(event);
        final String errorCodeMessage = resolveErrorCodeMessage(event);
        final String requestMethod = safeText(event.requestMethod(), "UNKNOWN");
        final String requestPath = safeText(event.requestPath(), "UNKNOWN");

        return "예외=%s | 에러명=%s | 코드=%s | msg=%s | 메시지=%s | 메서드=%s | 경로=%s".formatted(
                errorName,
                errorCodeName,
                errorCode,
                errorCodeMessage,
                errorMessage,
                requestMethod,
                requestPath
        );
    }

    private String resolveErrorMessage(ExceptionEvent event) {
        if (event == null) {
            return "UNKNOWN";
        }
        final String detail = safeText(SensitiveLogMessageSanitizer.sanitize(event.errorDetailMsg()), "");
        if (!detail.isBlank()) {
            return detail;
        }
        if (event.errorCode() == null) {
            return "UNKNOWN";
        }
        return safeText(event.errorCode().getErrorMessage(), "UNKNOWN");
    }

    private String resolveErrorCodeMessage(ExceptionEvent event) {
        if (event == null || event.errorCode() == null) {
            return "UNKNOWN";
        }
        return safeText(event.errorCode().getErrorMessage(), "UNKNOWN");
    }

    private boolean shouldRecordExceptionActivity(ExceptionEvent event) {
        if (event == null) {
            return false;
        }
        if (!memberGuard.isAuthenticated()) {
            return false;
        }

        final var account = event.account();
        if (account == null) {
            return false;
        }
        if (account.id() == null || account.id() < MIN_RECORDABLE_MEMBER_ID) {
            return false;
        }
        return account.loginId() != null && !account.loginId().isBlank();
    }

    private String safeText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }
}
