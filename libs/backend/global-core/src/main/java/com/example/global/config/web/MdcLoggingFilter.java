package com.example.global.config.web;

import com.example.global.logging.RequestContextScope;
import com.example.global.utils.TraceIdUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * MDC 로깅 필터
 * - 모든 HTTP 요청의 시작점에 고유한 Trace ID를 발급하여 MDC에 저장합니다.
 * - 이 ID는 Controller, Service, DB 로그 등 해당 요청의 전체 수명주기 동안 유지됩니다.
 * - 가장 먼저 실행되도록 최고 우선순위(HIGHEST_PRECEDENCE)를 부여합니다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final String traceId = resolveTraceId(request);

        // MDC에 traceId 저장 (Logback 설정이나 application.yml logging.pattern에서 %X{traceId}로 사용 가능)
        MDC.put(TraceIdUtils.TRACE_ID_KEY, traceId);

        try {
            if (response instanceof HttpServletResponse httpServletResponse) {
                httpServletResponse.setHeader(TraceIdUtils.resolveTraceHeaderName(), traceId);
            }
            doFilterWithRequestScope(request, response, chain);
        } finally {
            // 요청 처리 후 반드시 MDC 정리 (스레드 풀 재사용 시 데이터 오염 방지)
            MDC.clear();
        }
    }

    private void doFilterWithRequestScope(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            RequestContextScope.withRequest(httpServletRequest).call(() -> {
                chain.doFilter(request, response);
                return null;
            });
        } catch (IOException | ServletException e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException("요청 컨텍스트 바인딩 중 오류가 발생했습니다.", e);
        }
    }

    private String resolveTraceId(ServletRequest request) {
        if (request instanceof HttpServletRequest httpServletRequest) {
            return TraceIdUtils.resolveTraceIdFromRequest(httpServletRequest);
        }
        return TraceIdUtils.createTraceId();
    }
}
