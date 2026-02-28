package com.example.domain.security.guard.support;

import com.example.domain.account.enums.AccountRole;

import java.util.List;
import java.util.Optional;

/**
 * Security Guard 도메인이 회원 접근 대상 정보를 조회할 때 사용하는 Port
 *
 * <p>
 * - Security 도메인은 MemberRepository / Member 엔티티를 직접 참조하지 않고 이 Port를 통해 접근합니다.
 * - Port 정의: security/guard/support (사용하는 도메인)
 * - Adapter 구현: member/support (제공하는 도메인)
 * </p>
 */
public interface SecurityMemberAccessPort {

    /**
     * 회원 ID로 접근 대상 정보를 조회합니다.
     */
    Optional<MemberAccessTarget> findAccessTargetById(Long memberId);

    /**
     * 회원 ID와 역할 목록으로 접근 대상 정보를 조회합니다.
     */
    Optional<MemberAccessTarget> findAccessTargetByIdAndRoleIn(Long memberId, List<AccountRole> roles);
}
