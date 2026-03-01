package com.example.global.config.web;

import com.example.global.config.web.support.FallbackRequestLoggingSupport;
import com.example.global.exception.enums.ErrorCode;
import com.example.global.exception.support.ExceptionLogTemplates;
import com.example.global.utils.ClientIpExtractor;
import com.example.global.utils.SensitiveLogMessageSanitizer;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 컨트롤러(AOP) 로깅을 타지 못하는 요청에 대한 "fallback" 액세스 로그 필터
 *
 * <p>
 * [왜 필요한가]
 * - 로그인/인증/인가 실패 등은 Spring Security Filter 단계에서 응답이 끝나 ControllerLoggingAspect가 실행되지 않습니다.
 * - 이 경우에도 서버 로그(특히 traceId 기반 상관관계)가 남아야 운영에서 원인 추적이 가능합니다.
 * <p>
 * [동작 정책]
 * - {@link com.example.global.aop.ControllerLoggingAspect}가 로깅을 수행한 요청은 중복 로그를 방지하기 위해 스킵합니다.
 * - 그 외 요청(필터에서 종료된 요청)은 상태 코드 기반으로 요약 로그를 남깁니다.
 * - 민감정보(password/token 등)는 로깅하지 않습니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class FallbackRequestLoggingFilter extends OncePerRequestFilter {
    private final FallbackRequestLoggingSupport loggingSupport;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (request == null) {
            return true;
        }

        final String uri = request.getRequestURI();
        if (!hasText(uri)) {
            return true;
        }

        // API 요청만 "fallback" 로깅 대상으로 제한합니다.
        // (정적 리소스 등은 @RestController AOP 로깅 대상이 아니어서 과도한 로그가 발생할 수 있습니다.)
        return !uri.startsWith("/api");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (request == null || response == null) {
            filterChain.doFilter(request, response);
            return;
        }

        final long startedAt = System.currentTimeMillis();
        Throwable thrown = null;
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            thrown = e;
            throw e;
        } catch (Error e) {
            thrown = e;
            throw e;
        } finally {
            logIfNotControllerLogged(request, response, startedAt, thrown);
        }
    }

    private void logIfNotControllerLogged(
            HttpServletRequest request, HttpServletResponse response,
            long startedAt, Throwable thrown
    ) {
        if (shouldSkipLogging(request)) {
            return;
        }

        final String traceId = TraceIdUtils.resolveTraceId();
        final String ip = ClientIpExtractor.extract(request);
        final String loginId = loggingSupport.resolveLoginIdForLog(request);
        final String method = safe(request.getMethod());
        final String uri = loggingSupport.buildUriWithQuery(request);
        final int status = response != null ? response.getStatus() : 0;
        final long elapsedMs = Math.max(0, System.currentTimeMillis() - startedAt);

        request.setAttribute(RequestLoggingAttributes.FILTER_LOGGED, Boolean.TRUE);

        if (thrown != null) {
            logThrownException(traceId, ip, loginId, method, uri, status, elapsedMs, thrown);
            loggingSupport.publishExceptionEvent(request, thrown);
            return;
        }

        logByStatus(status, traceId, ip, loginId, method, uri, elapsedMs);
    }

    private boolean shouldSkipLogging(HttpServletRequest request) {
        if (request == null) {
            return true;
        }
        return Boolean.TRUE.equals(request.getAttribute(RequestLoggingAttributes.CONTROLLER_LOGGED))
                || Boolean.TRUE.equals(request.getAttribute(RequestLoggingAttributes.FILTER_LOGGED));
    }

    private void logThrownException(
            String traceId, String ip, String loginId, String method, String uri,
            int status, long elapsedMs, Throwable thrown
    ) {
        log.error(
                ExceptionLogTemplates.FILTER_EXCEPTION_LOG_TEMPLATE.stripTrailing(),
                traceId, ip, loginId, method, uri, status, elapsedMs,
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getErrorMessage(),
                thrown.getClass().getSimpleName(),
                sanitizeMessage(thrown.getMessage()),
                thrown
        );
    }

    private void logByStatus(
            int status, String traceId, String ip, String loginId,
            String method, String uri, long elapsedMs
    ) {
        final String template = ExceptionLogTemplates.FILTER_LOG_TEMPLATE.stripTrailing();

        if (status >= 500) {
            log.error(template, traceId, ip, loginId, method, uri, status, elapsedMs);
        } else if (status >= 400) {
            log.warn(template, traceId, ip, loginId, method, uri, status, elapsedMs);
        } else {
            log.info(template, traceId, ip, loginId, method, uri, status, elapsedMs);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String sanitizeMessage(String value) {
        return safe(SensitiveLogMessageSanitizer.sanitize(value));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
