package com.example.domain.security.guard.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberAccessTargetResolver {

    private final SecurityMemberAccessPort securityMemberAccessPort;

    public Optional<MemberAccessTarget> resolve(Long targetId) {
        if (targetId == null || targetId <= 0) {
            return Optional.empty();
        }

        return securityMemberAccessPort.findAccessTargetById(targetId);
    }
}
