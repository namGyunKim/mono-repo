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

        // 이 요청은 필터 레벨에서 이미 상세 원인(errors 포함)을 로깅했음을 표시합니다.
        // -> FallbackRequestLoggingFilter가 같은 요청을 다시 요약 로그로 남기지 않도록 중복 방지
        if (request != null) {
            request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);
        }

        // 필터 단계에서 컨트롤러 로깅(AOP)을 타지 못하므로, 실패 응답은 반드시 서버 로그에 남깁니다.
        String traceId = TraceIdUtils.resolveTraceId();
        String ip = ClientIpExtractor.extract(request);
        String method = LoginLoggingUtils.safe(request != null ? request.getMethod() : null);
        String uri = LoginLoggingUtils.safe(request != null ? request.getRequestURI() : null);
        String loginId = LoginLoggingUtils.resolveLoginIdOrDefault(
                request,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID,
                JsonBodyLoginAuthenticationFilter.REQUEST_ATTRIBUTE_LOGIN_ID,
                "UNKNOWN"
        );

        String code = errorCode != null ? errorCode.getCode() : "";
        String message = errorCode != null ? errorCode.getErrorMessage() : "";
        String errorName = errorCode != null ? errorCode.name() : "";
        String formattedErrors = LoginLoggingUtils.formatErrors(errors);

        log.warn(
                ExceptionLogTemplates.LOGIN_JSON_BAD_REQUEST_LOG_TEMPLATE.stripTrailing(),
                traceId,
                ip,
                method,
                uri,
                loginId,
                errorName,
                code,
                message,
                formattedErrors
        );

        try {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + "; charset=" + StandardCharsets.UTF_8.name());

            ApiErrorResponse body = ApiErrorResponse.of(code, message, traceId, errors);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (IOException ignored) {
            // 응답 쓰기 실패 시 추가 처리하지 않습니다.
        }
    }
}
