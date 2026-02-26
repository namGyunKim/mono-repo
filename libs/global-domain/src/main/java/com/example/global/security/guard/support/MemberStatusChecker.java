package com.example.global.security.guard.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.repository.MemberRepository;
import com.example.global.enums.GlobalActiveEnums;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MemberStatusChecker {

    private final MemberRepository memberRepository;

    public boolean isActiveMember(Long memberId, AccountRole role) {
        if (memberId == null || role == null) {
            return false;
        }

        return memberRepository.findByIdAndRoleIn(memberId, List.of(role))
                .map(member -> member.getActive() == GlobalActiveEnums.ACTIVE)
                .orElse(false);
    }
}
