package com.example.domain.member.support;

/**
 * member → security 도메인 경계를 넘는 권한 확인 포트
 * <p>
 * AdminMemberCommandService가 MemberGuard에
 * 직접 의존하지 않도록 추상화합니다.
 */
public interface MemberPermissionCheckPort {

    boolean isSameMember(Long targetMemberId);
}
