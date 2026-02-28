package com.example.global.config.web.support;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.security.guard.MemberGuard;
import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogWriter;
import com.example.global.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class ApiVersionErrorResponder {

    private final ObjectMapper objectMapper;
    private final ExceptionLogWriter exceptionLogWriter;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberGuard memberGuard;

    public void writeErrorResponse(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) throws IOException {
        if (response == null || response.isCommitted()) {
            return;
        }

        markFilterLogged(request);
        logVersionError(request, errorCode);
        publishExceptionEvent(request, errorCode);

        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

        final ApiErrorResponse body = ApiErrorResponse.from(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void logVersionError(HttpServletRequest request, ErrorCode errorCode) {
        if (request == null || errorCode == null) {
            return;
        }

        exceptionLogWriter.logMessageOnly(
                exceptionLogWriter.resolveRequestMeta(request),
                errorCode,
                errorCode.getErrorMessage()
        );
    }

    private void publishExceptionEvent(HttpServletRequest request, ErrorCode errorCode) {
        if (request == null || errorCode == null) {
            return;
        }

        final CurrentAccountDTO account = memberGuard.getCurrentAccountOrGuest();
        final String detailMessage = errorCode.getErrorMessage();

        applicationEventPublisher.publishEvent(ExceptionEvent.from(
                new IllegalArgumentException(detailMessage),
                errorCode,
                detailMessage,
                account,
                request
        ));
    }

    private void markFilterLogged(HttpServletRequest request) {
        if (request == null) {
            return;
        }
        request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
    }
}
