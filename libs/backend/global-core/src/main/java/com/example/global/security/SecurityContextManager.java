package com.example.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * SecurityContext 접근을 중앙에서 관리하는 컴포넌트
 * <p>
 * - SecurityContextHolder 직접 접근을 한 곳으로 모아 책임을 분리합니다.
 * - Controller/Service에서 SecurityContextHolder를 직접 호출하지 않도록 보호합니다.
 * </p>
 */
@Component
public class SecurityContextManager {

    public SecurityContext createEmptyContext() {
        return SecurityContextHolder.createEmptyContext();
    }

    public SecurityContext getContext() {
        return SecurityContextHolder.getContext();
    }

    public void setContext(SecurityContext securityContext) {
        if (securityContext == null) {
            SecurityContextHolder.clearContext();
            return;
        }
        SecurityContextHolder.setContext(securityContext);
    }

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public void clearContext() {
        SecurityContextHolder.clearContext();
    }
}
