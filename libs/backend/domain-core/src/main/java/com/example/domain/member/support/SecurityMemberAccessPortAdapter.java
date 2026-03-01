package com.example.domain.member.support;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.member.entity.Member;
import com.example.domain.member.enums.MemberActiveStatus;
import com.example.domain.member.repository.MemberRepository;
import com.example.domain.security.guard.support.MemberAccessTarget;
import com.example.domain.security.guard.support.SecurityMemberAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * SecurityMemberAccessPort 어댑터 — Member 도메인이 제공
 */
@Component
@RequiredArgsConstructor
public class SecurityMemberAccessPortAdapter implements SecurityMemberAccessPort {

    private final MemberRepository memberRepository;

    @Override
    public Optional<MemberAccessTarget> findAccessTargetById(Long memberId) {
        if (memberId == null || memberId <= 0) {
            return Optional.empty();
        }
        return memberRepository.findById(memberId)
                .map(member -> MemberAccessTarget.of(member.getRole(), member.getId(), member.getActive()));
    }

    @Override
    public Optional<MemberAccessTarget> findAccessTargetByIdAndRoleIn(Long memberId, List<AccountRole> roles) {
        if (memberId == null || memberId <= 0 || roles == null || roles.isEmpty()) {
            return Optional.empty();
        }
        return memberRepository.findByIdAndRoleIn(memberId, roles)
                .map(member -> MemberAccessTarget.of(member.getRole(), member.getId(), member.getActive()));
    }

    @Override
    public Optional<AccountAuthMemberView> findActiveAuthMemberByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return Optional.empty();
        }
        return memberRepository.findByLoginId(loginId)
                .filter(member -> member.getActive() == MemberActiveStatus.ACTIVE)
                .map(this::toAuthMemberView);
    }

    @Override
    public Optional<Long> findMemberIdByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return Optional.empty();
        }
        return memberRepository.findByLoginId(loginId)
                .map(Member::getId);
    }

    private AccountAuthMemberView toAuthMemberView(Member member) {
        return AccountAuthMemberView.of(
                member.getId(),
                member.getLoginId(),
                member.getPassword(),
                member.getNickName(),
                member.getRole(),
                member.getMemberType(),
                member.getActive(),
                member.getTokenVersion()
        );
    }
}
