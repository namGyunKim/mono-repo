package com.example.global.aop.support;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerLoggingSupport {

    private final ControllerParamsFormatter paramsFormatter;

    public String formatParams(ProceedingJoinPoint joinPoint) {
        return paramsFormatter.formatParams(joinPoint);
    }

    public String buildRequestLog(
            String traceId,
            String ip,
            String loginId,
            String method,
            String uri,
            String params
    ) {
        return ControllerLogMessageFactory.buildRequestLog(traceId, ip, loginId, method, uri, params);
    }

    public String buildResponseLog(String traceId, long elapsedMs, String status, String size) {
        return ControllerLogMessageFactory.buildResponseLog(traceId, elapsedMs, status, size);
    }

    public String getResponseStatus(HttpServletResponse response) {
        return ControllerResponseInfoResolver.getResponseStatus(response);
    }

    public String getResponseSize(HttpServletResponse response) {
        return ControllerResponseInfoResolver.getResponseSize(response);
    }
}
