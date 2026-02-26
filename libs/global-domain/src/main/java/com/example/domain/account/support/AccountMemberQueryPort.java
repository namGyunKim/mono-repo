package com.example.domain.account.support;

import com.example.domain.account.payload.dto.*;

import java.util.Optional;

public interface AccountMemberQueryPort {

    Optional<LoginMemberView> findLoginMemberView(AccountLoginIdRoleQuery query);

    Optional<AccountAuthMemberView> findAuthMember(AccountLoginIdQuery query);

    Optional<AccountAuthMemberView> findAuthMember(AccountLoginIdRoleQuery query);

    Optional<AccountLoginCandidateView> findLoginCandidate(AccountLoginValidationQuery query);
}
