package com.example.global.security.handler;

import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogTemplates;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증 실패(401) 처리 EntryPoint
 * <p>
 * - REST API 전용으로 JSON 응답을 반환합니다.
 * - 로그인 실패(아이디/비밀번호 불일치) 응답은 CustomAuthFailureHandler에서 처리합니다.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response, final AuthenticationException authException) throws IOException {
        if (response.isCommitted()) {
            return;
        }

        if (request != null) {
            request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
        }

        log.warn(
                ExceptionLogTemplates.AUTHENTICATION_ENTRYPOINT_LOG_TEMPLATE.stripTrailing(),
                TraceIdUtils.resolveTraceId(),
                ClientIpExtractor.extract(request),
                request != null ? request.getMethod() : "",
                request != null ? request.getRequestURI() : "",
                ErrorCode.AUTHENTICATION_REQUIRED.name(),
                ErrorCode.AUTHENTICATION_REQUIRED.getCode(),
                ErrorCode.AUTHENTICATION_REQUIRED.getErrorMessage(),
                authException != null ? authException.getMessage() : ""
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

        final ApiErrorResponse body = ApiErrorResponse.from(ErrorCode.AUTHENTICATION_REQUIRED);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
