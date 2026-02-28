package com.example.domain.security.guard.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.enums.MemberActiveStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberStatusChecker {

    private final SecurityMemberAccessPort securityMemberAccessPort;

    public boolean isActiveMember(Long memberId, AccountRole role) {
        if (memberId == null || role == null) {
            return false;
        }

        return securityMemberAccessPort.findAccessTargetByIdAndRoleIn(memberId, List.of(role))
                .map(target -> target.active() == MemberActiveStatus.ACTIVE)
                .orElse(false);
    }
}
