package com.example.global.aop.support;

import org.springframework.stereotype.Component;

@Component
public class ControllerLogMessageFactory {

    public String buildRequestLog(
            String traceId,
            String ip,
            String loginId,
            String method,
            String uri,
            String params
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

    public String buildResponseLog(String traceId, long elapsedMs, String status, String size) {
        return """
                [RES] [%s]
                Time    : %dms
                Status  : %s
                Size    : %s
                """.formatted(traceId, elapsedMs, status, size).stripTrailing();
    }
}
