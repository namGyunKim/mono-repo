package com.example.global.config.web.support;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.security.guard.MemberGuard;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.utils.SensitiveLogMessageSanitizer;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FallbackExceptionEventPublisher {

    private final MemberGuard memberGuard;
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishExceptionEvent(HttpServletRequest request, Throwable thrown) {
        if (request == null || thrown == null) {
            return;
        }

        final Exception exception = (thrown instanceof Exception casted) ? casted : new RuntimeException("Unhandled error", thrown);
        final CurrentAccountDTO account = memberGuard.getCurrentAccountOrGuest();
        final String detailMessage = thrown.getMessage() != null ? thrown.getMessage() : "필터 처리 중 예외 발생";
        final String sanitizedDetailMessage = SensitiveLogMessageSanitizer.sanitize(detailMessage);

        applicationEventPublisher.publishEvent(ExceptionEvent.from(
                exception,
                ErrorCode.INTERNAL_SERVER_ERROR,
                sanitizedDetailMessage,
                account,
                request
        ));
    }
}
