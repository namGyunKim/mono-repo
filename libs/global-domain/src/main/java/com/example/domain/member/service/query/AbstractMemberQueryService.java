package com.example.domain.member.service.query;

import com.example.domain.account.enums.AccountRole;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public abstract class AbstractMemberQueryService implements MemberQueryService {

    // 해당 서비스가 처리할 수 있는 권한 목록 반환
    public abstract List<AccountRole> getSupportedRoles();
}
