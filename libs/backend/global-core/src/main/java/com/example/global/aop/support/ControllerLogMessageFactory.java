package com.example.global.aop.support;

import org.springframework.stereotype.Component;

@Component
public class ControllerLogMessageFactory {

    public String buildRequestLog(
            final String traceId,
            final String ip,
            final String loginId,
            final String method,
            final String uri,
            final String params
    ) {
        return """
                [REQ] [%s]
                IP      : %s
                User    : %s
                Method  : %s
                URI     : %s
                Params  :
                %s
                """.formatted(traceId, ip, loginId, method, uri, params).stripTrailing();
    }

    public String buildResponseLog(final String traceId, final long elapsedMs, final String status, final String size) {
        return """
                [RES] [%s]
                Time    : %dms
                Status  : %s
                Size    : %s
                """.formatted(traceId, elapsedMs, status, size).stripTrailing();
    }
}
