package com.example.domain.member.support;

import com.example.domain.account.payload.dto.*;
import com.example.domain.account.support.AccountMemberQueryPort;
import com.example.domain.member.entity.Member;
import com.example.domain.member.repository.LoginMemberViewProjection;
import com.example.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AccountMemberQueryPortAdapter implements AccountMemberQueryPort {

    private final MemberRepository memberRepository;

    @Override
    public Optional<LoginMemberView> findLoginMemberView(AccountLoginIdRoleQuery query) {
        if (!isValidLoginIdRoleQuery(query)) {
            return Optional.empty();
        }
        return memberRepository.findProjectedByLoginIdAndRole(query.loginId(), query.role())
                .map(this::toLoginMemberView);
    }

    @Override
    public Optional<AccountAuthMemberView> findAuthMember(AccountLoginIdQuery query) {
        if (query == null || !StringUtils.hasText(query.loginId())) {
            return Optional.empty();
        }
        return memberRepository.findByLoginId(query.loginId())
                .map(this::toAuthMemberView);
    }

    @Override
    public Optional<AccountAuthMemberView> findAuthMember(AccountLoginIdRoleQuery query) {
        if (!isValidLoginIdRoleQuery(query)) {
            return Optional.empty();
        }
        return memberRepository.findByLoginIdAndRole(query.loginId(), query.role())
                .map(this::toAuthMemberView);
    }

    @Override
    public Optional<AccountLoginCandidateView> findLoginCandidate(AccountLoginValidationQuery query) {
        if (!isValidLoginValidationQuery(query)) {
            return Optional.empty();
        }
        return memberRepository.findByLoginIdAndRoleIn(query.loginId(), query.allowedRoles())
                .map(this::toLoginCandidateView);
    }

    private LoginMemberView toLoginMemberView(LoginMemberViewProjection projection) {
        return LoginMemberView.of(
                projection.getId(),
                projection.getLoginId(),
                projection.getRole(),
                projection.getNickName(),
                projection.getMemberType(),
                projection.getActive()
        );
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

    private AccountLoginCandidateView toLoginCandidateView(Member member) {
        return AccountLoginCandidateView.of(
                member.getId(),
                member.getLoginId(),
                member.getRole(),
                member.getMemberType(),
                member.getActive()
        );
    }

    private boolean isValidLoginIdRoleQuery(AccountLoginIdRoleQuery query) {
        return query != null
                && StringUtils.hasText(query.loginId())
                && query.role() != null;
    }

    private boolean isValidLoginValidationQuery(AccountLoginValidationQuery query) {
        return query != null
                && StringUtils.hasText(query.loginId())
                && query.allowedRoles() != null
                && !query.allowedRoles().isEmpty();
    }
}
