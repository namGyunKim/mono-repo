package com.example.domain.security.adapter;

import com.example.domain.member.support.MemberPermissionCheckPort;
import com.example.domain.security.guard.MemberGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberPermissionCheckPortAdapter implements MemberPermissionCheckPort {

    private final MemberGuard memberGuard;

    @Override
    public boolean isSameMember(Long targetMemberId) {
        return memberGuard.isSameMember(targetMemberId);
    }
}
