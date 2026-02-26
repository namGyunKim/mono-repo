package com.example.global.aop.support;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ControllerLoggingSupport {

    private final ControllerParamsFormatter paramsFormatter;
    private final ControllerLogMessageFactory logMessageFactory;
    private final ControllerResponseInfoResolver responseInfoResolver;

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
        return logMessageFactory.buildRequestLog(traceId, ip, loginId, method, uri, params);
    }

    public String buildResponseLog(String traceId, long elapsedMs, String status, String size) {
        return logMessageFactory.buildResponseLog(traceId, elapsedMs, status, size);
    }

    public String getResponseStatus(HttpServletResponse response) {
        return responseInfoResolver.getResponseStatus(response);
    }

    public String getResponseSize(HttpServletResponse response) {
        return responseInfoResolver.getResponseSize(response);
    }
}
