package com.example.domain.account.support;

import com.example.domain.account.payload.dto.AccountAuthMemberView;
import com.example.domain.account.payload.dto.AccountLoginCandidateView;
import com.example.domain.account.payload.dto.AccountLoginIdQuery;
import com.example.domain.account.payload.dto.AccountLoginIdRoleQuery;
import com.example.domain.account.payload.dto.AccountLoginValidationQuery;
import com.example.domain.account.payload.dto.LoginMemberView;

import java.util.Optional;

public interface AccountMemberQueryPort {

    Optional<LoginMemberView> findLoginMemberView(AccountLoginIdRoleQuery query);

    Optional<AccountAuthMemberView> findAuthMember(AccountLoginIdQuery query);

    Optional<AccountAuthMemberView> findAuthMember(AccountLoginIdRoleQuery query);

    Optional<AccountLoginCandidateView> findLoginCandidate(AccountLoginValidationQuery query);
}
