package com.example.global.config.jpa;

import com.example.global.security.SecurityContextManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
@RequiredArgsConstructor
public class JpaAuditConfig {

    private static final String SYSTEM_AUDITOR = "SYSTEM";

    private final SecurityContextManager securityContextManager;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = securityContextManager.getAuthentication();
            if (!isAuthenticated(authentication)) {
                return Optional.of(SYSTEM_AUDITOR);
            }

            String principalName = authentication.getName();
            if (principalName == null || principalName.isBlank()) {
                return Optional.of(SYSTEM_AUDITOR);
            }

            return Optional.of(principalName);
        };
    }

    private boolean isAuthenticated(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        if (!authentication.isAuthenticated()) {
            return false;
        }
        return !(authentication instanceof AnonymousAuthenticationToken);
    }
}
