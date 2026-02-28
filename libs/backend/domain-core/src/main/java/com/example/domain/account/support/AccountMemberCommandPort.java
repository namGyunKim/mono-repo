package com.example.domain.account.support;

import com.example.domain.account.enums.AccountRole;

/**
 * account → member 도메인 경계를 넘는 회원 커맨드 포트
 * <p>
 * AccountCommandService가 MemberStrategyFactory / MemberCommandService에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface AccountMemberCommandPort {

    Long updateMemberProfile(AccountRole role, String nickName, String password, Long memberId);

    void deactivateMember(AccountRole role, Long memberId);
}
