package com.example.global.security.handler;

import com.example.domain.account.payload.dto.CurrentAccountDTO;
import com.example.domain.log.event.ExceptionEvent;
import com.example.domain.security.guard.MemberGuard;
import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.payload.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 접근 거부(403) 처리 핸들러
 * <p>
 * - REST API 전용으로 JSON 응답을 반환합니다.
 * - 기존 ExceptionAdvice에서 수행하던 예외 로깅(Event 발행) 흐름을 유지합니다.
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MemberGuard memberGuard;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        publishAccessDeniedEvent(request, accessDeniedException, resolveMessage(accessDeniedException));

        if (response.isCommitted()) {
            return;
        }

        if (request != null) {
            request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

        ApiErrorResponse body = ApiErrorResponse.from(ErrorCode.ACCESS_DENIED);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private void publishAccessDeniedEvent(HttpServletRequest request, AccessDeniedException e, String message) {
        if (request == null || e == null) {
            return;
        }

        CurrentAccountDTO currentAccount = resolveCurrentAccount();
        applicationEventPublisher.publishEvent(
                ExceptionEvent.from(e, ErrorCode.ACCESS_DENIED, message, currentAccount, request)
        );
    }

    private CurrentAccountDTO resolveCurrentAccount() {
        return memberGuard.getCurrentAccount().orElse(null);
    }

    private String resolveMessage(AccessDeniedException e) {
        if (e == null) {
            return "권한이 없습니다.";
        }
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return "권한이 없습니다.";
        }
        return msg;
    }

}
