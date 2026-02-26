package com.example.domain.member.service.query;

import com.example.domain.account.enums.AccountRole;
import com.example.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserMemberQueryService extends AbstractQueryDslMemberQueryService {

    public UserMemberQueryService(MemberRepository memberRepository) {
        super(memberRepository);
    }

    @Override
    public List<AccountRole> getSupportedRoles() {
        return List.of(AccountRole.USER);
    }
}
