package com.example.global.security.filter.support;

import com.example.global.config.web.RequestLoggingAttributes;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogTemplates;
import com.example.global.payload.response.ApiErrorDetail;
import com.example.global.payload.response.ApiErrorResponse;
import com.example.global.security.filter.JsonBodyLoginAuthenticationFilter;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.LoginLoggingUtils;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonBodyLoginErrorWriter {

    private final ObjectMapper objectMapper;

    public void writeBadRequest(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode, List<ApiErrorDetail> errors) {
        if (response == null || response.isCommitted()) {
            return;
        }

        markFilterLogged(request);

        final String traceId = TraceIdUtils.resolveTraceId();
        logBadRequest(request, traceId, errorCode, errors);
        writeJsonResponse(response, traceId, errorCode, errors);
    }

    private void markFilterLogged(HttpServletRequest request) {
        if (request != null) {
            request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
        }
    }

    private void logBadRequest(HttpServletRequest request, String traceId, ErrorCode errorCode, List<ApiErrorDetail> errors) {
        final String ip = ClientIpExtractor.extract(request);
        final String method = LoginLoggingUtils.safe(request != null ? request.getMethod() : null);
        final String uri = LoginLoggingUtils.safe(request != null ? request.getRequestURI() : null);
        final String loginId = LoginLoggingUtils.resolveLoginIdOrDefault(
                request,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID,
                LoginLoggingUtils.DEFAULT_UNKNOWN_LOGIN_ID
        );

        final String code = errorCode != null ? errorCode.getCode() : "";
        final String message = errorCode != null ? errorCode.getErrorMessage() : "";
        final String errorName = errorCode != null ? errorCode.name() : "";
        final String formattedErrors = LoginLoggingUtils.formatErrors(errors);

        log.warn(
                ExceptionLogTemplates.LOGIN_JSON_BAD_REQUEST_LOG_TEMPLATE.stripTrailing(),
                traceId, ip, method, uri, loginId,
                errorName, code, message, formattedErrors
        );
    }

    private void writeJsonResponse(HttpServletResponse response, String traceId, ErrorCode errorCode, List<ApiErrorDetail> errors) {
        final String code = errorCode != null ? errorCode.getCode() : "";
        final String message = errorCode != null ? errorCode.getErrorMessage() : "";

        try {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

            final ApiErrorResponse body = ApiErrorResponse.of(code, message, traceId, errors);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (IOException ignored) {
            // 응답 쓰기 실패 시 추가 처리하지 않습니다.
        }
    }
}
