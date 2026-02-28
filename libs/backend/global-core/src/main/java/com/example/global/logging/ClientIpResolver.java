package com.example.global.logging;

import com.example.global.utils.ClientIpExtractor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 현재 요청 컨텍스트 기반 클라이언트 IP 조회 컴포넌트
 *
 * <p>
 * - HttpServletRequest 의존을 도메인 서비스 밖으로 분리하기 위해 제공됩니다.
 * - 요청 컨텍스트가 없으면 "UNKNOWN"을 반환합니다.
 * </p>
 */
@Component
public class ClientIpResolver {

    public String resolveClientIp() {
        final HttpServletRequest request = resolveRequest();
        return ClientIpExtractor.extract(request);
    }

    private HttpServletRequest resolveRequest() {
        final HttpServletRequest scopedRequest = RequestContextScope.getCurrentRequestOrNull();
        if (scopedRequest != null) {
            return scopedRequest;
        }

        return resolveRequestFromContextHolder();
    }

    private HttpServletRequest resolveRequestFromContextHolder() {
        final RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return null;
        }

        return servletAttributes.getRequest();
    }
}
