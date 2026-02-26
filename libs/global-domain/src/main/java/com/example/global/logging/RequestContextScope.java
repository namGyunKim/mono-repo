package com.example.global.logging;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 요청 수명주기 동안 HttpServletRequest를 ScopedValue로 전달하기 위한 컨텍스트
 *
 * <p>
 * - Java 25 권장사항에 따라 요청 컨텍스트 전달 시 ThreadLocal 대안으로 ScopedValue를 우선 사용합니다.
 * - 프레임워크 내부 요청 컨텍스트(RequestContextHolder)가 필요한 경우를 위해 fallback은 별도 처리합니다.
 * </p>
 */
public final class RequestContextScope {

    private static final ScopedValue<HttpServletRequest> CURRENT_REQUEST = ScopedValue.newInstance();

    private RequestContextScope() {
    }

    public static ScopedValue.Carrier withRequest(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request는 필수입니다.");
        }
        return ScopedValue.where(CURRENT_REQUEST, request);
    }

    public static HttpServletRequest getCurrentRequestOrNull() {
        if (!CURRENT_REQUEST.isBound()) {
            return null;
        }
        return CURRENT_REQUEST.get();
    }
}
