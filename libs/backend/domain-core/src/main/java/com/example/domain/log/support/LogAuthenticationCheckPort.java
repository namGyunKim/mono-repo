package com.example.domain.log.support;

/**
 * log → security 도메인 경계를 넘는 인증 상태 확인 포트
 * <p>
 * ExceptionMemberActivityEventListener가 MemberGuard에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface LogAuthenticationCheckPort {

    boolean isAuthenticated();
}
