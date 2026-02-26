package com.example.global.config.jpa;

import com.example.domain.security.guard.MemberGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * JPA Auditing 설정
 * - 엔티티의 생성자(@CreatedBy)/수정자(@LastModifiedBy) 처리 로직을 정의합니다.
 */
@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class JpaAuditConfig {

    private final MemberGuard memberGuard;

    /**
     * 현재 로그인한 사용자의 LoginId를 반환합니다.
     * 로그인하지 않은 상태(초기화, 회원가입 등)에서는 "SYSTEM"을 반환하여 DB에 null이 들어가는 것을 방지합니다.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // 1. 인증 정보가 없거나 익명 사용자(로그인 전)인 경우 -> "SYSTEM" 처리
            if (!memberGuard.isAuthenticated()) {
                // return Optional.empty(); // 기존: null 저장
                return Optional.of("SYSTEM"); // 수정: "SYSTEM" 저장 (데이터 식별 용이)
            }

            // 2. 인증된 사용자의 loginId 반환
            return Optional.of(memberGuard.getLoginIdOrDefault("SYSTEM"));
        };
    }
}
