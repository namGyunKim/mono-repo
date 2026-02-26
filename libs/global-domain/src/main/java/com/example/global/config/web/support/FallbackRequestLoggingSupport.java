package com.example.global.config.web.support;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FallbackRequestLoggingSupport {

    private final FallbackLoginIdResolver loginIdResolver;
    private final FallbackRequestUriBuilder requestUriBuilder;
    private final FallbackExceptionEventPublisher exceptionEventPublisher;

    /**
     * 로그에 노출 가능한 loginId를 최대한 확보합니다.
     *
     * <p>
     * - 인증된 사용자는 MemberGuard 경유로 loginId를 조회
     * - 로그인 시도는 파라미터/Request Attribute(loginId) 우선
     * </p>
     */
    public String resolveLoginIdForLog(HttpServletRequest request) {
        return loginIdResolver.resolveLoginId(request);
    }

    public String buildUriWithQuery(HttpServletRequest request) {
        return requestUriBuilder.buildUriWithQuery(request);
    }

    public void publishExceptionEvent(HttpServletRequest request, Throwable thrown) {
        exceptionEventPublisher.publishExceptionEvent(request, thrown);
    }
}
