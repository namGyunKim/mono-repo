package com.example.global.security.service.query;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.member.payload.dto.MemberLoginIdQuery;
import com.example.domain.security.guard.support.SecurityMemberAccessPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberAuthQueryService {

    private final SecurityMemberAccessPort securityMemberAccessPort;

    public Optional<AccountAuthMemberView> findActiveMemberForAuthentication(MemberLoginIdQuery query) {
        if (!isValidQuery(query)) {
            return Optional.empty();
        }

        return securityMemberAccessPort.findActiveAuthMemberByLoginId(query.loginId());
    }

    public Optional<Long> findMemberIdByLoginId(MemberLoginIdQuery query) {
        if (!isValidQuery(query)) {
            return Optional.empty();
        }

        return securityMemberAccessPort.findMemberIdByLoginId(query.loginId());
    }

    private boolean isValidQuery(MemberLoginIdQuery query) {
        return query != null && StringUtils.hasText(query.loginId());
    }
}
